package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.AIRangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.utils.LogHelper;
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
import net.minecraft.util.Tuple;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target.")
public class SkeletonAIFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<String> minMaxShootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> avoidPlayerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> arrowInaccuracyConfig;
	private final BlacklistConfig entityBlacklistConfig;

	private static final String minMaxShootingRangeDefault = "24,48";

	public Tuple<Integer, Integer> minMaxShootingRange;
	public double avoidPlayerChance = 0.5d;
	public double arrowInaccuracy = 2;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public SkeletonAIFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		minMaxShootingRangeConfig = Config.builder
				.comment("The min and max range from where a skeleton will shoot a player. (NO SPACES)")
				.define("Min Max Shooting Range", minMaxShootingRangeDefault);
		avoidPlayerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to avoid the player")
				.defineInRange("Avoid Player chance", this.avoidPlayerChance, 0d, 1d);
		arrowInaccuracyConfig = Config.builder
				.comment("How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
				.defineInRange("Arrow Inaccuracy", this.arrowInaccuracy, 0d, 30d);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the enhanced Shoot AI", Arrays.asList("quark:forgotten"), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minMaxShootingRange = parseIntTuple(this.minMaxShootingRangeConfig.get());
		this.avoidPlayerChance = this.avoidPlayerChanceConfig.get();
		this.arrowInaccuracy = this.arrowInaccuracyConfig.get();
		this.entityBlacklist = IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	private Tuple<Integer, Integer> parseIntTuple(String s) {
		String[] split = s.split(",");
		if (split.length != 2)
			return new Tuple<>(Integer.parseInt(minMaxShootingRangeDefault.split(",")[0]), Integer.parseInt(minMaxShootingRangeDefault.split(",")[1]));
		if (!NumberUtils.isParsable(split[0])) {
			LogHelper.warn("Invalid number for Min Max Shooting Range");
			return new Tuple<>(Integer.parseInt(minMaxShootingRangeDefault.split(",")[0]), Integer.parseInt(minMaxShootingRangeDefault.split(",")[1]));
		}
		int a = Integer.parseInt(split[0]);
		if (!NumberUtils.isParsable(split[1])) {
			LogHelper.warn("Invalid number for Min Max Shooting Range");
			return new Tuple<>(Integer.parseInt(minMaxShootingRangeDefault.split(",")[0]), Integer.parseInt(minMaxShootingRangeDefault.split(",")[1]));
		}
		int b = Integer.parseInt(split[1]);
		if (a > b)
			a = b;
		return new Tuple<>(a, b);
	}

	//TODO Zombies with Ender Pearls
	public void setCombatTask(AbstractSkeletonEntity skeleton) {
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
		for (PrioritizedGoal prioritizedGoal : skeleton.goalSelector.goals) {
			if (prioritizedGoal.getGoal().equals(skeleton.aiArrowAttack))
				hasAIArrowAttack = true;
		}
		List<Goal> avoidEntityGoals = skeleton.goalSelector.goals.stream()
				.map(PrioritizedGoal::getGoal)
				.filter(g -> g instanceof AIAvoidEntityGoal<?>)
				.collect(Collectors.toList());

		avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
		if (hasAIArrowAttack) {
			AIRangedBowAttackGoal<AbstractSkeletonEntity> rangedBowAttackGoal = new AIRangedBowAttackGoal<>(skeleton, 1.0d, 20, RandomHelper.getInt(skeleton.world.rand, minMaxShootingRange.getA(), minMaxShootingRange.getB()));
			skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);

			if (skeleton.world.rand.nextDouble() < this.avoidPlayerChance) {
				AIAvoidEntityGoal<PlayerEntity> avoidEntityGoal = new AIAvoidEntityGoal<>(skeleton, PlayerEntity.class, 12.0f, 1.5d, 1.25d);
				skeleton.goalSelector.addGoal(1, avoidEntityGoal);
			}
		}
	}
}