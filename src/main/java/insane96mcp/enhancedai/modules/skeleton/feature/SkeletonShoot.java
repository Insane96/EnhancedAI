package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.EARangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target.")
public class SkeletonShoot extends Feature {

	private final MinMax.Config shootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> strafeChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> arrowInaccuracyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> spammerChanceConfig;
	private final Blacklist.Config entityBlacklistConfig;

	private final List<String> defaultBlacklist = List.of("quark:forgotten");

	public MinMax shootingRange = new MinMax(24, 32);
	public double strafeChance = 0.333d;
	public double arrowInaccuracy = 2;
	public double spammerChance = 0.07d;
	public Blacklist entityBlacklist;

	public SkeletonShoot(Module module) {
		super(Config.builder, module);
		super.pushConfig(Config.builder);
		this.shootingRangeConfig = new MinMax.Config(Config.builder, "Shooting Range", "The range from where a skeleton will shoot a player")
				.setMinMax(1, 64, this.shootingRange)
				.build();
		strafeChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
				.defineInRange("Strafe chance", this.strafeChance, 0d, 1d);
		arrowInaccuracyConfig = Config.builder
				.comment("How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
				.defineInRange("Arrow Inaccuracy", this.arrowInaccuracy, 0d, 30d);
		spammerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn as a spammer, which spams arrows instead of fully charging the bow")
				.defineInRange("Spammer chance", this.spammerChance, 0d, 1d);
		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the enhanced Shoot AI")
				.setDefaultList(defaultBlacklist)
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.shootingRange = this.shootingRangeConfig.get();
		this.strafeChance = this.strafeChanceConfig.get();
		this.arrowInaccuracy = this.arrowInaccuracyConfig.get();
		this.spammerChance = this.spammerChanceConfig.get();
		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	public void onReassessWeaponGoal(AbstractSkeleton skeleton) {
		if (!this.isEnabled())
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(skeleton))
			return;

		boolean strafe = skeleton.level.random.nextDouble() < this.strafeChance;

		CompoundTag persistentData = skeleton.getPersistentData();

		if (persistentData.contains(Strings.Tags.Skeleton.STRAFE)) {
			strafe = persistentData.getBoolean(Strings.Tags.Skeleton.STRAFE);
		}
		else {
			persistentData.putBoolean(Strings.Tags.Skeleton.STRAFE, strafe);
		}

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
			int attackCooldown = 20;
			int bowChargeTicks = 20;
			double inaccuracy = this.arrowInaccuracy;
			if (skeleton.level.random.nextDouble() < this.spammerChance) {
				attackCooldown = 5;
				bowChargeTicks = 5;
				inaccuracy *= 2d;
			}
			if (!skeleton.level.getDifficulty().equals(Difficulty.HARD))
				attackCooldown *= 2;

			EARangedBowAttackGoal<AbstractSkeleton> EARangedBowAttackGoal = new EARangedBowAttackGoal<>(skeleton, 1.0d, this.shootingRange.getIntRandBetween(skeleton.getRandom()), strafe)
					.setAttackCooldown(attackCooldown)
					.setBowChargeTicks(bowChargeTicks)
					.setInaccuracy((float) inaccuracy);
			skeleton.goalSelector.addGoal(2, EARangedBowAttackGoal);
		}
	}
}