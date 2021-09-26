package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.AIRangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.utils.IdTagMatcher;
import insane96mcp.insanelib.utils.RandomHelper;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target.")
public class SkeletonAIFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> minShootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxShootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> avoidPlayerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> attackWhenAvoidingChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> strafeChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> arrowInaccuracyConfig;
	private final BlacklistConfig entityBlacklistConfig;

	public int minShootingRange = 24;
	public int maxShootingRange = 48;
	public double avoidPlayerChance = 0.5d;
	public double attackWhenAvoidingChance = 0.5d;
	public double strafeChance = 0.5d;
	public double arrowInaccuracy = 2;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public SkeletonAIFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		minShootingRangeConfig = Config.builder
				.comment("The min range from where a skeleton will shoot a player")
				.defineInRange("Min Shooting Range", this.minShootingRange, 1, 64);
		maxShootingRangeConfig = Config.builder
				.comment("The max range from where a skeleton will shoot a player")
				.defineInRange("Max Shooting Range", this.maxShootingRange, 1, 64);
		avoidPlayerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to avoid the player")
				.defineInRange("Avoid Player chance", this.avoidPlayerChance, 0d, 1d);
		attackWhenAvoidingChanceConfig = Config.builder
				.comment("Chance for a Skeleton to attack while running from a player")
				.defineInRange("Attack When Avoiding Chance", this.attackWhenAvoidingChance, 0d, 1d);
		strafeChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
				.defineInRange("Strafe chance", this.strafeChance, 0d, 1d);
		arrowInaccuracyConfig = Config.builder
				.comment("How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
				.defineInRange("Arrow Inaccuracy", this.arrowInaccuracy, 0d, 30d);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the enhanced Shoot AI", Arrays.asList("quark:forgotten"), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minShootingRange = this.minShootingRangeConfig.get();
		this.maxShootingRange = this.maxShootingRangeConfig.get();
		this.avoidPlayerChance = this.avoidPlayerChanceConfig.get();
		this.attackWhenAvoidingChance = this.attackWhenAvoidingChanceConfig.get();
		this.strafeChance = this.strafeChanceConfig.get();
		this.arrowInaccuracy = this.arrowInaccuracyConfig.get();
		this.entityBlacklist = IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	public void reassessWeaponGoal(AbstractSkeletonEntity skeleton) {
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

		boolean hasAIArrowAttack = false;
		for (PrioritizedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
			if (prioritizedGoal.getGoal().equals(skeleton.bowGoal))
				hasAIArrowAttack = true;
		}
		List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
				.map(PrioritizedGoal::getGoal)
				.filter(g -> g instanceof AIAvoidEntityGoal<?>)
				.collect(Collectors.toList());

		avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
		if (hasAIArrowAttack) {
			AIRangedBowAttackGoal<AbstractSkeletonEntity> rangedBowAttackGoal = new AIRangedBowAttackGoal<>(skeleton, 1.0d, 20, RandomHelper.getInt(skeleton.level.random, this.minShootingRange, this.maxShootingRange), skeleton.level.random.nextDouble() < this.strafeChance);
			skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);

			if (skeleton.level.random.nextDouble() < this.avoidPlayerChance) {
				AIAvoidEntityGoal<PlayerEntity> avoidEntityGoal = new AIAvoidEntityGoal<>(skeleton, PlayerEntity.class, 12.0f, 1.5d, 1.25d);
				avoidEntityGoal.setAttackWhenRunning(skeleton.level.random.nextDouble() < this.attackWhenAvoidingChance);
				skeleton.goalSelector.addGoal(1, avoidEntityGoal);
			}
		}
	}
}