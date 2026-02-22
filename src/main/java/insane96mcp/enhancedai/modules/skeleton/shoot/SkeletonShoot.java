package insane96mcp.enhancedai.modules.skeleton.shoot;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.Difficulty;
import insane96mcp.insanelib.core.feature.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@LoadFeature(module = EAIModules.Ids.SKELETON, description = "Skeletons are more precise when shooting and can hit a target farther away. Only entity types in `enhancedai:skeleton/better_shooting` tag are affected by this feature")
public class SkeletonShoot extends Feature {

	public static final TagKey<EntityType<?>> BETTER_SKELETON_SHOOT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("skeleton/better_shooting"));

	@Config(min = 1, max = 64, description = "The range from where a skeleton will shoot a player")
	public static MinMax shootingRange = new MinMax(24, 32);
	@Config(min = 0, description = "The ticks cooldown after shooting")
	public static MinMax shootingCooldown = new MinMax(35, 40);
	@Config(min = 0, description = "The ticks the skeleton charges the bow. at least 20 ticks for a full charge.")
	public static MinMax bowChargeTicks = new MinMax(15, 30);
	@Config(min = 0d, max = 1d, description = "Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
	public static Double strafeChance = 0.333d;
	@Config(min = 0d, max = 30d, description = "How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
	public static Difficulty inaccuracy = new Difficulty(6, 5, 3);

    public static EAIData<Integer> SHOOTING_RANGE;
	public static EAIData<Integer> SHOOTING_COOLDOWN;
    public static EAIData<Integer> BOW_CHARGE_TICKS;
    public static EAIData<Boolean> STRAFE;
    public static EAIData<Double> INACCURACY;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
        SHOOTING_RANGE = EAIData.ofInt(this.createDataKey("shooting_range"));
        SHOOTING_COOLDOWN = EAIData.ofInt(this.createDataKey("shooting_cooldown"));
        BOW_CHARGE_TICKS = EAIData.ofInt(this.createDataKey("bow_charge_ticks"));
        STRAFE = EAIData.ofBool(this.createDataKey("strafe"));
        INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
    }

	public static void onReassessWeaponGoal(AbstractSkeleton skeleton) {
		if (!isEnabled(SkeletonShoot.class)
                || Spawning.isUnaffectedByFeatures(skeleton)
				|| skeleton.level().isClientSide
				|| !skeleton.getType().is(BETTER_SKELETON_SHOOT))
			return;

        GoalHelper.removeGoal(skeleton.goalSelector, EAIRangedBowAttackGoal.class);
		if (GoalHelper.hasGoal(skeleton.goalSelector, skeleton.bowGoal)) {
			skeleton.goalSelector.removeGoal(skeleton.bowGoal);
			EAIRangedBowAttackGoal rangedBowAttackGoal = new EAIRangedBowAttackGoal(skeleton, 1.0d, SHOOTING_COOLDOWN, INACCURACY, SHOOTING_RANGE, STRAFE, BOW_CHARGE_TICKS);
			skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);
		}
		SHOOTING_COOLDOWN.applyIfAbsent(skeleton, shootingCooldown.getIntRandBetween(skeleton.getRandom()));
		INACCURACY.applyIfAbsent(skeleton, inaccuracy.getByDifficulty(skeleton.level()));
		SHOOTING_RANGE.applyIfAbsent(skeleton, shootingRange.getIntRandBetween(skeleton.getRandom()));
		STRAFE.applyIfAbsent(skeleton, skeleton.getRandom().nextDouble() < strafeChance);
		BOW_CHARGE_TICKS.applyIfAbsent(skeleton, bowChargeTicks.getIntRandBetween(skeleton.getRandom()));
	}
}