package insane96mcp.enhancedai.module.mobs.lunge;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.MOBS, description = "Melee mobs stop for a moment and then lunge at their target when in range. Only entity types in `enhancedai:mobs/can_lunge` tag are affected by this feature, and only while they're attacking in melee.")
public class LungeAtTarget extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.id("mobs/can_lunge"));

	@Config(min = 0d, max = 1d, description = "Chance for a mob to be able to lunge. Rolled when the mob spawns.")
	public static Double chance = 1d;
	@Config(min = 0d, max = 32d, description = "Minimum distance from the target to lunge")
	public static Double minDistance = 3d;
	@Config(min = 0d, max = 32d, description = "Maximum distance from the target to lunge")
	public static Double maxDistance = 5d;
	@Config(min = 0d, max = 6000d, description = "Ticks after landing before the mob can lunge again. Set when the mob spawns, based on the difficulty.")
	public static DifficultyBasedConfig cooldown = new DifficultyBasedConfig(160, 100, 80);
	@Config(min = 0, max = 100, description = "Ticks the mob stands still before lunging. Set to 0 to lunge immediately.")
	public static Integer windUpTicks = 8;
	@Config(min = 0d, max = 10d, description = "Horizontal strength is calculated so that the mob lands in front of the target, then multiplied by this value.")
	public static Double horizontalStrengthMultiplier = 1d;
	@Config(min = 0d, max = 4d, description = "Vertical speed of the lunge")
	public static Double verticalStrength = 0.35d;

	public static EAIData<Boolean> CAN_LUNGE;
	public static EAIData<Double> MIN_DISTANCE;
	public static EAIData<Double> MAX_DISTANCE;
	public static EAIData<Integer> COOLDOWN;
	public static EAIData<Integer> WIND_UP_TICKS;
	public static EAIData<Double> HORIZONTAL_STRENGTH_MULTIPLIER;
	public static EAIData<Double> VERTICAL_STRENGTH;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		CAN_LUNGE = EAIData.ofBool(this.createDataKey("can_lunge"), (mob, canLunge) -> {
			GoalHelper.removeGoal(mob.goalSelector, LungeAtTargetGoal.class);
			if (canLunge)
				mob.goalSelector.addGoal(1, new LungeAtTargetGoal(mob));
		});
		MIN_DISTANCE = EAIData.ofDouble(this.createDataKey("min_distance"));
		MAX_DISTANCE = EAIData.ofDouble(this.createDataKey("max_distance"));
		COOLDOWN = EAIData.ofInt(this.createDataKey("cooldown"));
		WIND_UP_TICKS = EAIData.ofInt(this.createDataKey("wind_up_ticks"));
		HORIZONTAL_STRENGTH_MULTIPLIER = EAIData.ofDouble(this.createDataKey("horizontal_strength_multiplier"));
		VERTICAL_STRENGTH = EAIData.ofDouble(this.createDataKey("vertical_strength"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		MIN_DISTANCE.applyIfAbsent(mob, minDistance);
		MAX_DISTANCE.applyIfAbsent(mob, maxDistance);
		COOLDOWN.applyIfAbsent(mob, (int) cooldown.getByDifficulty(mob.level()));
		WIND_UP_TICKS.applyIfAbsent(mob, windUpTicks);
		HORIZONTAL_STRENGTH_MULTIPLIER.applyIfAbsent(mob, horizontalStrengthMultiplier);
		VERTICAL_STRENGTH.applyIfAbsent(mob, verticalStrength);
		CAN_LUNGE.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
	}

	/// True if the mob is standing still preparing to lunge. Used to prevent the melee goal from moving the mob.
	public static boolean isWindingUp(Mob mob) {
		return GoalHelper.getGoal(mob.goalSelector, LungeAtTargetGoal.class)
				.map(LungeAtTargetGoal::isWindingUp)
				.orElse(false);
	}
}
