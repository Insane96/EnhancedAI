package insane96mcp.enhancedai.modules.skeleton.shoot;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoalLegacy;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;

import java.util.List;

@LoadFeature(module = Modules.Ids.SKELETON, description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target. Use the enhancedai:better_skeleton_shoot entity type tag to add more skeletons that are affected by this feature")
public class SkeletonShoot extends Feature {

	public static final TagKey<EntityType<?>> BETTER_SKELETON_SHOOT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("better_skeleton_shoot"));

	public static final String STRAFE = EnhancedAI.RESOURCE_PREFIX + "strafe";
	public static final String SHOOTING_RANGE = EnhancedAI.RESOURCE_PREFIX + "shooting_range";
	public static final String SHOOTING_COOLDOWN = EnhancedAI.RESOURCE_PREFIX + "shooting_cooldown";
	public static final String BOW_CHARGE_TICKS = EnhancedAI.RESOURCE_PREFIX + "bow_charge_ticks";
	public static final String INACCURACY = EnhancedAI.RESOURCE_PREFIX + "inaccuracy";
	private static final String SPAMMER = EnhancedAI.RESOURCE_PREFIX + "spammer";

	@Config(min = 1, max = 64, description = "The range from where a skeleton will shoot a player")
	public static MinMax shootingRange = new MinMax(24, 32);
	@Config(min = 0, description = "The ticks cooldown after shooting")
	public static MinMax shootingCooldown = new MinMax(35, 40);
	@Config(min = 0, description = "The ticks the skeleton charges the bow. at least 20 ticks for a full charge.")
	public static MinMax bowChargeTicks = new MinMax(15, 30);
	@Config(min = 0d, max = 1d, description = "Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
	public static Double strafeChance = 0.333d;
	@Config(min = 0d, max = 30d, description = "How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
	public static insane96mcp.insanelib.base.config.Difficulty arrowInaccuracy = new insane96mcp.insanelib.base.config.Difficulty(6, 5, 3);
	@Config(min = 0d, max = 1d, description = "Chance for a Skeleton to spawn as a spammer, which spams arrows instead of fully charging the bow")
	public static Double spammerChance = 0.07d;

	public SkeletonShoot(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void onReassessWeaponGoal(AbstractSkeleton skeleton) {
		if (!isEnabled(SkeletonShoot.class)
				|| !skeleton.getType().is(BETTER_SKELETON_SHOOT))
			return;

		CompoundTag persistentData = skeleton.getPersistentData();

		boolean strafe = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, STRAFE, skeleton.getRandom().nextDouble() < strafeChance);
		int shootingRange1 = NBTUtils.getIntOrPutDefaultLegacy(persistentData, SHOOTING_RANGE, shootingRange.getIntRandBetween(skeleton.getRandom()));
		double inaccuracy = NBTUtils.getDoubleOrPutDefaultLegacy(persistentData, INACCURACY, arrowInaccuracy.getByDifficulty(skeleton.level()));
		boolean spammer = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, SPAMMER, skeleton.getRandom().nextDouble() < spammerChance);
		int shootingCooldown1 = NBTUtils.getIntOrPutDefaultLegacy(persistentData, SHOOTING_COOLDOWN, shootingCooldown.getIntRandBetween(skeleton.getRandom()));
		int bowChargeTicks1 = NBTUtils.getIntOrPutDefaultLegacy(persistentData, BOW_CHARGE_TICKS, bowChargeTicks.getIntRandBetween(skeleton.getRandom()));

		boolean hasAIArrowAttack = false;
		for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
            if (prioritizedGoal.getGoal().equals(skeleton.bowGoal)) {
                hasAIArrowAttack = true;
                break;
            }
		}
		List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
				.map(WrappedGoal::getGoal)
				.filter(g -> g instanceof EAAvoidEntityGoalLegacy<?>)
				.toList();

		avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
		if (hasAIArrowAttack) {
			if (spammer) {
				shootingCooldown1 = 30;
				bowChargeTicks1 = 1;
				inaccuracy *= 2.5d;
			}

			EARangedBowAttackGoal rangedBowAttackGoal = (EARangedBowAttackGoal) new EARangedBowAttackGoal(skeleton, 1.0d, shootingRange1, strafe)
					.setBowChargeTicks(bowChargeTicks1)
					.setAttackCooldown(shootingCooldown1)
					.setInaccuracy((float) inaccuracy);
			skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);
		}
	}
}