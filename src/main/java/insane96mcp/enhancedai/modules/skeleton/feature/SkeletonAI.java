package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.AIRangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import insane96mcp.insanelib.util.RandomHelper;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target.")
public class SkeletonAI extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> minShootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxShootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> strafeChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> arrowInaccuracyConfig;
	private final BlacklistConfig entityBlacklistConfig;
	//Flee from target
	private final ForgeConfigSpec.ConfigValue<Double> avoidPlayerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> attackWhenAvoidingChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> fleeDistanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> fleeDistanceNearConfig;
	private final ForgeConfigSpec.ConfigValue<Double> fleeSpeedNearConfig;
	private final ForgeConfigSpec.ConfigValue<Double> fleeSpeedFarConfig;

	private final List<String> defaultBlacklist = Arrays.asList("quark:forgotten");

	public int minShootingRange = 24;
	public int maxShootingRange = 48;
	public double strafeChance = 0.333d;
	public double arrowInaccuracy = 2;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;
	//Flee from target
	public double avoidPlayerChance = 0.5d;
	public double attackWhenAvoidingChance = 0.5d;
	public double fleeDistance = 16;
	public double fleeDistanceNear = 8;
	public double fleeSpeedNear = 1.6d;
	public double fleeSpeedFar = 1.3d;

	public SkeletonAI(Module module) {
		super(Config.builder, module);
		super.pushConfig(Config.builder);
		minShootingRangeConfig = Config.builder
				.comment("The min range from where a skeleton will shoot a player")
				.defineInRange("Min Shooting Range", this.minShootingRange, 1, 64);
		maxShootingRangeConfig = Config.builder
				.comment("The max range from where a skeleton will shoot a player")
				.defineInRange("Max Shooting Range", this.maxShootingRange, 1, 64);
		strafeChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
				.defineInRange("Strafe chance", this.strafeChance, 0d, 1d);
		arrowInaccuracyConfig = Config.builder
				.comment("How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
				.defineInRange("Arrow Inaccuracy", this.arrowInaccuracy, 0d, 30d);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the enhanced Shoot AI", defaultBlacklist, false);

		Config.builder.push("Flee from Target");
		avoidPlayerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to avoid the player")
				.defineInRange("Avoid Player chance", this.avoidPlayerChance, 0d, 1d);
		attackWhenAvoidingChanceConfig = Config.builder
				.comment("Chance for a Skeleton to be able to shoot while running from a player")
				.defineInRange("Attack When Avoiding Chance", this.attackWhenAvoidingChance, 0d, 1d);
		fleeDistanceConfig = Config.builder
				.comment("Distance from a player that will make the skeleton run away.")
				.defineInRange("Flee Distance", this.fleeDistance, 0d, 32d);
		fleeDistanceNearConfig = Config.builder
				.comment("Distance from a player that counts as near and will make the skeleton run away faster.")
				.defineInRange("Flee Distance Near", this.fleeDistanceNear, 0d, 32d);
		fleeSpeedFarConfig = Config.builder
				.comment("Speed multiplier when the skeleton avoids the player and it's farther than 'Flee Distance Near' blocks from him.")
				.defineInRange("Flee speed Multiplier Far", this.fleeSpeedFar, 0d, 4d);
		fleeSpeedNearConfig = Config.builder
				.comment("Speed multiplier when the skeleton avoids the player and it's within 'Flee Distance Near' blocks from him.")
				.defineInRange("Flee speed Multiplier Near", this.fleeSpeedNear, 0d, 4d);
		Config.builder.pop(2);
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minShootingRange = this.minShootingRangeConfig.get();
		this.maxShootingRange = this.maxShootingRangeConfig.get();
		this.strafeChance = this.strafeChanceConfig.get();
		this.arrowInaccuracy = this.arrowInaccuracyConfig.get();
		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
		//Flee from target
		this.avoidPlayerChance = this.avoidPlayerChanceConfig.get();
		this.attackWhenAvoidingChance = this.attackWhenAvoidingChanceConfig.get();
		this.fleeDistance = this.fleeDistanceConfig.get();
		this.fleeDistanceNear = this.fleeDistanceNearConfig.get();
		this.fleeSpeedNear = this.fleeSpeedNearConfig.get();
		this.fleeSpeedFar = this.fleeSpeedFarConfig.get();
	}

	public void reassessWeaponGoal(AbstractSkeleton skeleton) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
			if (blacklistEntry.matchesEntity(skeleton)) {
				if (!this.entityBlacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}
		if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
			return;

		boolean processed = skeleton.getPersistentData().getBoolean(Strings.Tags.PROCESSED);

		boolean strafe = skeleton.level.random.nextDouble() < this.strafeChance;
		boolean avoidTarget = skeleton.level.random.nextDouble() < this.avoidPlayerChance;
		boolean attackWhenAvoiding = skeleton.level.random.nextDouble() < this.attackWhenAvoidingChance;

		if (processed) {
			strafe = skeleton.getPersistentData().getBoolean(Strings.Tags.Skeleton.STRAFE);
			avoidTarget = skeleton.getPersistentData().getBoolean(Strings.Tags.Skeleton.AVOID_TARGET);
			attackWhenAvoiding = skeleton.getPersistentData().getBoolean(Strings.Tags.Skeleton.ATTACK_WHEN_AVOIDING);
		}
		else {
			skeleton.getPersistentData().putBoolean(Strings.Tags.Skeleton.STRAFE, strafe);
			skeleton.getPersistentData().putBoolean(Strings.Tags.Skeleton.AVOID_TARGET, avoidTarget);
			skeleton.getPersistentData().putBoolean(Strings.Tags.Skeleton.ATTACK_WHEN_AVOIDING, attackWhenAvoiding);
			skeleton.getPersistentData().putBoolean(Strings.Tags.PROCESSED, true);
		}

		boolean hasAIArrowAttack = false;
		for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
			if (prioritizedGoal.getGoal().equals(skeleton.bowGoal))
				hasAIArrowAttack = true;
		}
		List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
				.map(WrappedGoal::getGoal)
				.filter(g -> g instanceof AIAvoidEntityGoal<?>)
				.toList();

		avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
		if (hasAIArrowAttack) {
			AIRangedBowAttackGoal<AbstractSkeleton> rangedBowAttackGoal = new AIRangedBowAttackGoal<>(skeleton, 1.0d, 20, RandomHelper.getInt(skeleton.level.random, this.minShootingRange, this.maxShootingRange), strafe);
			skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);

			if (avoidTarget) {
				AIAvoidEntityGoal<Player> avoidEntityGoal = new AIAvoidEntityGoal<>(skeleton, Player.class, (float) this.fleeDistance, (float) this.fleeDistanceNear, this.fleeSpeedNear, this.fleeSpeedFar);
				avoidEntityGoal.setAttackWhenRunning(attackWhenAvoiding);
				skeleton.goalSelector.addGoal(1, avoidEntityGoal);
			}
		}
	}
}