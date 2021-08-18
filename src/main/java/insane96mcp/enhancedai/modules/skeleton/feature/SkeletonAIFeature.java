package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.AIRangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.stream.Collectors;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from 64 blocks and try to stay away from the target.")
public class SkeletonAIFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> avoidPlayerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> arrowInaccuracyConfig;
	//private final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistConfig;

	public double avoidPlayerChance = 1d;
	public double arrowInaccuracy = 0;

	public SkeletonAIFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		avoidPlayerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to avoid the player")
				.defineInRange("Avoid Player chance", this.avoidPlayerChance, 0d, 1d);
		arrowInaccuracyConfig = Config.builder
				.comment("How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
				.defineInRange("Arrow Inaccuracy", this.arrowInaccuracy, 0d, 30d);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.avoidPlayerChance = this.avoidPlayerChanceConfig.get();
		this.arrowInaccuracy = this.arrowInaccuracyConfig.get();
	}

	//TODO Zombies with Ender Pearls
	public void setCombatTask(AbstractSkeletonEntity skeleton) {
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
			AIRangedBowAttackGoal<AbstractSkeletonEntity> rangedBowAttackGoal = new AIRangedBowAttackGoal<>(skeleton, 1.0d, 20, 64.0f);
			skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);

			if (skeleton.world.rand.nextDouble() < this.avoidPlayerChance) {
				AIAvoidEntityGoal<PlayerEntity> avoidEntityGoal = new AIAvoidEntityGoal<>(skeleton, PlayerEntity.class, 12.0f, 1.6d, 1.3d);
				skeleton.goalSelector.addGoal(1, avoidEntityGoal);
			}
		}
	}
}