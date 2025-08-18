package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class TargetingLegacy extends JsonFeature {
	public static final TagKey<EntityType<?>> USE_TARGET_CHANGES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("use_target_changes"));

	public static final String IS_NEUTRAL = EnhancedAI.RESOURCE_PREFIX + "is_neutral";

	public static final List<CustomHostileConfig> CUSTOM_HOSTILE_DEFAULT_LIST = List.of(
			new CustomHostileConfig(2, IdTagMatcher.newTag("enhancedai:config/can_attack_villagers"), IdTagMatcher.newId("minecraft:villager"), 0.5f),
			new CustomHostileConfig(2, IdTagMatcher.newTag("enhancedai:config/can_attack_iron_golem"), IdTagMatcher.newId("minecraft:iron_golem"), 0.5f)
	);

	public static final List<CustomHostileConfig> customHostile = new ArrayList<>();

	@Config(description = "By default, the new targeting AI only changes for player targeting. Setting this to true allows overriding target AI for entities other than players. Please note this might break specific AIs")
	public static Boolean targetingOverrideForNonPlayers = false;
	@Config(description = "Mobs will no longer take random time to target a player.")
	public static Boolean instantTarget = false;
    @Config(description = "Chance for a mob to be able to forget about it's target. If the mob can forget the target it will forget about it after 'Unseen forgot ticks' have passed.")
    public static Double forgetTargetChance = 0.1d;
    @Config(description = "If the mob can forget the target it will forget about it after this amount of ticks have passed while not seeing the target.")
    public static Integer unseenForgotTicks = 400;
	@Config(min = 0d, max = 1d, description = "Chances for a mob to spawn neutral")
	public static Difficulty neutralChances = new Difficulty(0.25d, 0.10d, 0.04d);

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("custom_hostile.json", customHostile, CUSTOM_HOSTILE_DEFAULT_LIST, CustomHostileConfig.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processTargetGoal(mob);
		processCustomTargetGoal(mob);
	}

	private void processTargetGoal(Mob mob) {
		if (!mob.getType().is(USE_TARGET_CHANGES))
			return;
		List<WrappedGoal> goalsToAdd = new ArrayList<>();

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> goal))
				continue;

			if (goal.targetType != Player.class && !targetingOverrideForNonPlayers)
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());

			boolean isNeutral = NBTUtils.getBooleanOrPutDefaultLegacy(mob.getPersistentData(), IS_NEUTRAL, mob.getRandom().nextDouble() < neutralChances.getByDifficulty(mob.level()));
			if (isNeutral)
				continue;

			EAINearestAttackableTarget<? extends LivingEntity> newTargetGoal;

			if (mob instanceof Spider)
				newTargetGoal = new EAISpiderTargetGoal<>((Spider) mob, goal.targetType, true, true, goal.targetConditions);
			else
				newTargetGoal = new EAINearestAttackableTarget<>(mob, goal.targetType, false, true, goal.targetConditions);

			if (instantTarget)
				newTargetGoal.setInstaTarget();

			goalsToAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newTargetGoal));
		}

		goalsToRemove.forEach(mob.targetSelector::removeGoal);
		goalsToAdd.forEach(wrappedGoal -> mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));

	}

	private void processCustomTargetGoal(Mob mob) {
		if (customHostile.isEmpty())
			return;
		for (CustomHostileConfig chc : customHostile) {
			if (!chc.attacker.matchesEntity(mob) || mob.getRandom().nextFloat() > chc.chance)
				continue;

			EAINearestAttackableTarget<LivingEntity> targetGoal = new EAINearestAttackableTarget<>(mob, LivingEntity.class, chc.victim, chc.mustSee, false, TargetingConditions.forCombat());

			if (instantTarget)
				targetGoal.setInstaTarget();
			mob.targetSelector.addGoal(chc.priority, targetGoal);
		}
	}
}
