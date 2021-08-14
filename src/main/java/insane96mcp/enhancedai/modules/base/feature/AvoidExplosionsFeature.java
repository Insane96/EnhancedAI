package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidExplosionGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.CreatureEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Avoid Explosions", description = "Mobs will run away from exploding creepers / TNT")
public class AvoidExplosionsFeature extends Feature {

	public AvoidExplosionsFeature(Module module) {
		super(Config.builder, module);
		//Config.builder.comment(this.getDescription()).push(this.getName());
		//Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof CreatureEntity))
			return;

		CreatureEntity creatureEntity = (CreatureEntity) event.getEntity();

		//TODO skeletons run away if too near the player
		/*boolean hasTargetGoal = false;

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (PrioritizedGoal prioritizedGoal : creatureEntity.targetSelector.goals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal))
				continue;

			NearestAttackableTargetGoal goal = (NearestAttackableTargetGoal) prioritizedGoal.getGoal();

			if (goal.targetClass != PlayerEntity.class)
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());
			hasTargetGoal = true;
		}

		if (!hasTargetGoal)
			return;

		goalsToRemove.forEach(creatureEntity.goalSelector::removeGoal);
*/
		creatureEntity.goalSelector.addGoal(1, new AIAvoidExplosionGoal(creatureEntity, 1.75d, 1.3d));
	}
}
