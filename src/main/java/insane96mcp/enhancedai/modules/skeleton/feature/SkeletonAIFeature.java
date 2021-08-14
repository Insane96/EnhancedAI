package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.AIRangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting, can hit a target from 64 blocks and try to stay away from the target.")
public class SkeletonAIFeature extends Feature {

	//private final ForgeConfigSpec.ConfigValue<Double> spammerChanceConfig;

	//public double spammerChance = 0.01d;

	public SkeletonAIFeature(Module module) {
		super(Config.builder, module);
		/*Config.builder.comment(this.getDescription()).push(this.getName());
		spammerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn as a spammer")
				.defineInRange("Spammer chance", spammerChance, 0d, 1d);
		Config.builder.pop();*/
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		//spammerChance = spammerChanceConfig.get();
	}

	//TODO Zombies with Ender Pearls
	@SubscribeEvent
	public void eventEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof AbstractSkeletonEntity))
			return;

		AbstractSkeletonEntity skeleton = (AbstractSkeletonEntity) event.getEntity();

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		skeleton.goalSelector.goals.forEach(prioritizedGoal -> {
			if (prioritizedGoal.getGoal() instanceof RangedBowAttackGoal)
				goalsToRemove.add(prioritizedGoal.getGoal());
		});

		goalsToRemove.forEach(skeleton.goalSelector::removeGoal);
		AIRangedBowAttackGoal<AbstractSkeletonEntity> rangedBowAttackGoal = new AIRangedBowAttackGoal<>(skeleton, 1.0d, 20, 64.0f);
		skeleton.goalSelector.addGoal(2, rangedBowAttackGoal);

		AIAvoidEntityGoal<PlayerEntity> avoidEntityGoal = new AIAvoidEntityGoal<>(skeleton, PlayerEntity.class, 12.0f, 1.8d, 1.4d);
		//avoidEntityGoal.setAlwaysRun(true);
		skeleton.goalSelector.addGoal(1, avoidEntityGoal);
	}
}