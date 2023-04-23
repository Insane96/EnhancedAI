package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.EARangedBowAttackGoal;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;

import java.util.List;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target.")
@LoadFeature(module = Modules.Ids.SKELETON)
public class SkeletonShoot extends Feature {

	public static final String STRAFE = EnhancedAI.RESOURCE_PREFIX + "strafe";
	public static final String SHOOTING_RANGE = EnhancedAI.RESOURCE_PREFIX + "shooting_range";
	public static final String SHOOTING_COOLDOWN = EnhancedAI.RESOURCE_PREFIX + "shooting_cooldown";
	public static final String BOW_CHARGE_TICKS = EnhancedAI.RESOURCE_PREFIX + "bow_charge_ticks";
	public static final String INACCURACY = EnhancedAI.RESOURCE_PREFIX + "inaccuracy";
	private static final String SPAMMER = EnhancedAI.RESOURCE_PREFIX + "spammer";

	@Config(min = 1, max = 64)
	@Label(name = "Shooting Range", description = "The range from where a skeleton will shoot a player")
	public static MinMax shootingRange = new MinMax(24, 32);
	@Config(min = 0)
	@Label(name = "Shooting Cooldown", description = "The ticks cooldown after shooting. This is halved in Hard difficulty")
	public static MinMax shootingCooldown = new MinMax(40, 60);
	@Config(min = 0)
	@Label(name = "Bow charge ticks", description = "The ticks the skeleton charges the bow. 20 ticks for a full charge.")
	public static MinMax bowChargeTicks = new MinMax(15, 30);
	@Config(min = 0d, max = 1d)
	@Label(name = "Strafe chance", description = "Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
	public static Double strafeChance = 0.333d;
	@Config(min = 0d, max = 30d)
	@Label(name = "Arrow Inaccuracy", description = "How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
	public static Double arrowInaccuracy = 2d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Spammer chance", description = "Chance for a Skeleton to spawn as a spammer, which spams arrows instead of fully charging the bow")
	public static Double spammerChance = 0.07d;
	@Config
	@Label(name = "Entity Blacklist", description = "Entities that will not get affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(List.of(
			new IdTagMatcher(IdTagMatcher.Type.ID, "quark:forgotten")
	), false);

	public SkeletonShoot(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void onReassessWeaponGoal(AbstractSkeleton skeleton) {
		if (!isEnabled(SkeletonShoot.class)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(skeleton))
			return;

		CompoundTag persistentData = skeleton.getPersistentData();

		boolean strafe = NBTUtils.getBooleanOrPutDefault(persistentData, STRAFE, skeleton.getRandom().nextDouble() < strafeChance);
		int shootingRange1 = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_RANGE, shootingRange.getIntRandBetween(skeleton.getRandom()));
		double inaccuracy = NBTUtils.getDoubleOrPutDefault(persistentData, INACCURACY, arrowInaccuracy);
		boolean spammer = NBTUtils.getBooleanOrPutDefault(persistentData, SPAMMER, skeleton.getRandom().nextDouble() < spammerChance);
		int shootingCooldown1 = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_COOLDOWN, shootingCooldown.getIntRandBetween(skeleton.getRandom()));
		int bowChargeTicks1 = NBTUtils.getIntOrPutDefault(persistentData, BOW_CHARGE_TICKS, bowChargeTicks.getIntRandBetween(skeleton.getRandom()));

		boolean hasAIArrowAttack = false;
		for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
			if (prioritizedGoal.getGoal().equals(skeleton.bowGoal))
				hasAIArrowAttack = true;
		}
		List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
				.map(WrappedGoal::getGoal)
				.filter(g -> g instanceof EAAvoidEntityGoal<?>)
				.toList();

		avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
		if (hasAIArrowAttack) {
			if (spammer) {
				shootingCooldown1 = 30;
				bowChargeTicks1 = 1;
				inaccuracy *= 2.5d;
			}
			if (skeleton.level.getDifficulty().equals(Difficulty.HARD))
				shootingCooldown1 /= 2;

			EARangedBowAttackGoal<AbstractSkeleton> EARangedBowAttackGoal = new EARangedBowAttackGoal<>(skeleton, 1.0d, shootingRange1, strafe)
					.setAttackCooldown(shootingCooldown1)
					.setBowChargeTicks(bowChargeTicks1)
					.setInaccuracy((float) inaccuracy);
			skeleton.goalSelector.addGoal(2, EARangedBowAttackGoal);
		}
	}
}