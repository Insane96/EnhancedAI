package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class TargetingLegacy extends JsonFeature {

	public static final String IS_NEUTRAL = EnhancedAI.RESOURCE_PREFIX + "is_neutral";

	public static final List<CustomHostileConfig> CUSTOM_HOSTILE_DEFAULT_LIST = List.of(
			new CustomHostileConfig(2, IdTagMatcher.newTag("enhancedai:config/can_attack_villagers"), IdTagMatcher.newId("minecraft:villager"), 0.5f),
			new CustomHostileConfig(2, IdTagMatcher.newTag("enhancedai:config/can_attack_iron_golem"), IdTagMatcher.newId("minecraft:iron_golem"), 0.5f)
	);

	public static final List<CustomHostileConfig> customHostile = new ArrayList<>();

    @Config(description = "Chance for a mob to be able to forget about it's target. If the mob can forget the target it will forget about it after 'Unseen forgot ticks' have passed.")
    public static Double forgetTargetChance = 0.1d;
    @Config(description = "If the mob can forget the target it will forget about it after this amount of ticks have passed while not seeing the target.")
    public static Integer unseenForgotTicks = 400;

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

		processCustomTargetGoal(mob);
	}

	private void processCustomTargetGoal(Mob mob) {
		if (customHostile.isEmpty())
			return;
		for (CustomHostileConfig chc : customHostile) {
			if (!chc.attacker.matchesEntity(mob) || mob.getRandom().nextFloat() > chc.chance)
				continue;

			EAINearestAttackableTarget<LivingEntity> targetGoal = new EAINearestAttackableTarget<>(mob, LivingEntity.class, chc.victim, chc.mustSee, false, TargetingConditions.forCombat());

			mob.targetSelector.addGoal(chc.priority, targetGoal);
		}
	}
}
