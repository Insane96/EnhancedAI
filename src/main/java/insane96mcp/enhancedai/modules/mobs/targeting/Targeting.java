package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAHurtByTargetGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LoadFeature(module = Modules.Ids.MOBS, description = "Change how mobs target players. Use the enhancedai:use_target_changes and enhancedai:use_follow_range_changes entity type tag to whitelist mobs. Add mobs to enhancedai:allow_target_switch entity type tag to allow these mobs to be able to switch targets when hit (e.g. Creepers can't normally do that).")
public class Targeting extends JsonFeature {
	public static final TagKey<EntityType<?>> USE_TARGET_CHANGES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("use_target_changes"));
	public static final TagKey<EntityType<?>> CHANGE_FOLLOW_RANGE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("change_follow_range"));
	public static final TagKey<EntityType<?>> APPLY_XRAY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("apply_xray"));
	public static final TagKey<EntityType<?>> ALLOW_TARGET_SWITCH = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("allow_target_switch"));

	public static final String SPRINT = EnhancedAI.RESOURCE_PREFIX + "sprint";
	public static final String IS_NEUTRAL = EnhancedAI.RESOURCE_PREFIX + "is_neutral";
    public static final String FOLLOW_RANGES_PROCESSED = EnhancedAI.RESOURCE_PREFIX + "follow_ranges_processed";

	public static final List<CustomHostileConfig> CUSTOM_HOSTILE_DEFAULT_LIST = List.of(
			new CustomHostileConfig(2, IdTagMatcher.newTag("enhancedai:config/can_attack_villagers"), IdTagMatcher.newId("minecraft:villager"), 0.5f),
			new CustomHostileConfig(2, IdTagMatcher.newTag("enhancedai:config/can_attack_iron_golem"), IdTagMatcher.newId("minecraft:iron_golem"), 0.5f)
	);

	public static final List<CustomHostileConfig> customHostile = new ArrayList<>();

    @Config(min = 0d, max = 128d, description = "How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting 'Max' to 0 will leave the follow range as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute. Only mobs in the entity type tag `enhancedai:change_follow_range` will be affected by this override")
	public static MinMax followRangeOverride = new MinMax(24, 48);
	@Config(min = 0d, max = 128d, description = "How far away can the mobs see the player even through walls. Setting 'Max' to 0 will make mobs not able to see through walls. I recommend using mods like Mobs Properties Randomness to have more control over the attribute; the attribute name is 'enhancedai:generic.xray_follow_range'. Only mobs in the entity type tag `enhancedai:apply_xray` will be affected by this override.")
	public static MinMax xrayRangeOverride = new MinMax(12, 24);
	@Config(description = "By default, the new targeting AI only changes for targeting players. Setting this to true allows overriding target AI for entities other than players. Please note this might break specific AIs")
	public static Boolean targetingOverrideForNonPlayers = false;
	@Config(description = "Mobs will no longer take random time to target a player.")
	public static Boolean instantTarget = false;
    @Config(description = "Chance for a mob to be able to forget about it's target. If the mob can forget the target it will forget about it after 'Unseen forgot ticks' have passed.")
    public static Double forgetTargetChance = 0.1d;
    @Config(description = "If the mob can forget the target it will forget about it after this amount of ticks have passed while not seeing the target.")
    public static Integer unseenForgotTicks = 400;
	@Config(description = "Mobs will be able to find better paths to the target. Note that this might hit performance a bit.")
	public static Boolean betterPathfinding = true;
	@Config(description = "Mobs will actually switch target when attacked unless it's the same or if the current one it's closer.")
	public static Boolean betterHurtByTarget$enable = true;
	@Config(description = "Mobs will prefer to attack players instead of other mobs (Note that 'Prevent infighting' should be disabled).")
	public static Boolean betterHurtByTarget$preferPlayers = true;
	@Config(min = 0d, max = 1d, description = "Change for a mob to not attack other mobs when hit.")
	public static Double betterHurtByTarget$preventInfighting = 0.9d;
	@Config(min = 0d, max = 1d, description = "Chances for a mob to spawn neutral")
	public static Difficulty neutralChances = new Difficulty(0.25d, 0.10d, 0.04d);
	@Config(min = 0d, max = 1d, description = "If the mobs' affected by blindness effect the target range is multiplied by this value")
	public static Double blindnessRangeMultiplier = .1d;

	public Targeting(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("custom_hostile.json", customHostile, CUSTOM_HOSTILE_DEFAULT_LIST, CustomHostileConfig.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	public static void xrayRangeAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, EAAttributes.XRAY_FOLLOW_RANGE.get()))
				continue;

			event.add(entityType, EAAttributes.XRAY_FOLLOW_RANGE.get(), 0d);
		}
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processFollowRanges(mob);
		processTargetGoal(mob);
		processCustomTargetGoal(mob);
		processHurtByGoal(mob);

		boolean sprint = NBTUtils.getBooleanOrPutDefaultLegacy(mob.getPersistentData(), SPRINT, false);
		if (sprint)
			mob.setSprinting(true);
	}

	private void processHurtByGoal(Mob mob) {
		if (!betterHurtByTarget$enable
				|| !(mob instanceof PathfinderMob pathfinderMob)
				|| !pathfinderMob.getType().is(USE_TARGET_CHANGES))
			return;

		List<HurtByTargetGoal> toRemove = new ArrayList<>();
		List<WrappedGoal> toAdd = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : pathfinderMob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof HurtByTargetGoal goal))
				continue;
			toRemove.add(goal);

			List<Class<?>> toIgnoreDamage = new ArrayList<>(Arrays.asList(goal.toIgnoreDamage));
			//Prevent infighting
			if (betterHurtByTarget$preventInfighting > 0 && mob.getRandom().nextFloat() < betterHurtByTarget$preventInfighting && mob instanceof Enemy)
				toIgnoreDamage.add(Enemy.class);
			EAHurtByTargetGoal newGoal = new EAHurtByTargetGoal(pathfinderMob, toIgnoreDamage.toArray(Class[]::new));
			if (goal.toIgnoreAlert != null)
				newGoal.setAlertOthers(goal.toIgnoreAlert);
			toAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newGoal));
		}

		toAdd.forEach(wrappedGoal -> pathfinderMob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));
		if (!toRemove.isEmpty())
			toRemove.forEach(pathfinderMob.targetSelector::removeGoal);
		else if (mob.getType().is(ALLOW_TARGET_SWITCH)) {
			List<Class<?>> toIgnoreDamage = new ArrayList<>();
			//Prevent infighting
			if (betterHurtByTarget$preventInfighting > 0 && mob.getRandom().nextFloat() < betterHurtByTarget$preventInfighting)
				toIgnoreDamage.add(Enemy.class);
			EAHurtByTargetGoal newGoal = new EAHurtByTargetGoal(pathfinderMob, toIgnoreDamage.toArray(Class[]::new));
			pathfinderMob.targetSelector.addGoal(1, newGoal);
		}
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

			EANearestAttackableTarget<? extends LivingEntity> newTargetGoal;

			if (mob instanceof Spider)
				newTargetGoal = new EASpiderTargetGoal<>((Spider) mob, goal.targetType, true, true, goal.targetConditions);
			else
				newTargetGoal = new EANearestAttackableTarget<>(mob, goal.targetType, false, true, goal.targetConditions);

			if (instantTarget)
				newTargetGoal.setInstaTarget();

			goalsToAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newTargetGoal));
		}

		goalsToRemove.forEach(mob.targetSelector::removeGoal);
		goalsToAdd.forEach(wrappedGoal -> mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));

		if (betterPathfinding)
			mob.getNavigation().setMaxVisitedNodesMultiplier(4f);
	}

	private void processCustomTargetGoal(Mob mob) {
		if (customHostile.isEmpty())
			return;
		for (CustomHostileConfig chc : customHostile) {
			if (!chc.attacker.matchesEntity(mob) || mob.getRandom().nextFloat() > chc.chance)
				continue;

			EANearestAttackableTarget<LivingEntity> targetGoal = new EANearestAttackableTarget<>(mob, LivingEntity.class, chc.victim, chc.mustSee, false);

			if (instantTarget)
				targetGoal.setInstaTarget();
			mob.targetSelector.addGoal(chc.priority, targetGoal);
		}
	}

	private void processFollowRanges(Mob mob) {
		CompoundTag persistentData = mob.getPersistentData();
		if (!persistentData.getBoolean(FOLLOW_RANGES_PROCESSED)) {
			//noinspection ConstantConditions
			if (mob.getType().is(CHANGE_FOLLOW_RANGE) && followRangeOverride.min != 0d && mob.getAttribute(Attributes.FOLLOW_RANGE) != null && mob.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() < followRangeOverride.min) {
				MCUtils.setAttributeValue(mob, Attributes.FOLLOW_RANGE, followRangeOverride.getIntRandBetween(mob.getRandom()));
			}

			//noinspection ConstantConditions
			if (mob.getType().is(APPLY_XRAY) && xrayRangeOverride.min != 0d && mob.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()) != null && mob.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()).getBaseValue() < xrayRangeOverride.min) {
				MCUtils.setAttributeValue(mob, EAAttributes.XRAY_FOLLOW_RANGE.get(), xrayRangeOverride.getIntRandBetween(mob.getRandom()));
			}
			persistentData.putBoolean(FOLLOW_RANGES_PROCESSED, true);
		}
	}

	@SubscribeEvent
	public void onTargetDistanceMultiplier(LivingEvent.LivingVisibilityEvent event) {
		if (!this.isEnabled()
				|| blindnessRangeMultiplier == 1d)
			return;

		if (event.getLookingEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(MobEffects.BLINDNESS))
			event.modifyVisibility(blindnessRangeMultiplier);
	}
}
