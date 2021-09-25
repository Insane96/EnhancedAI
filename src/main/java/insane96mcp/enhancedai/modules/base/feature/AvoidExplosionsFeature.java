package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidExplosionGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Avoid Explosions", description = "Mobs will run away from exploding creepers / TNT")
public class AvoidExplosionsFeature extends Feature {

	public AvoidExplosionsFeature(Module module) {
		super(Config.builder, module);
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		addAvoidAI(event);
		alertTNT(event);
	}

	private void addAvoidAI(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof CreatureEntity))
			return;

		CreatureEntity creatureEntity = (CreatureEntity) event.getEntity();

		creatureEntity.goalSelector.addGoal(1, new AIAvoidExplosionGoal(creatureEntity, 1.6d, 1.3d));
	}

	private void alertTNT(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof TNTEntity))
			return;

		TNTEntity tnt = (TNTEntity) event.getEntity();
		List<CreatureEntity> creaturesNearby = tnt.level.getLoadedEntitiesOfClass(CreatureEntity.class, tnt.getBoundingBox().inflate(8d));
		for (CreatureEntity creatureEntity : creaturesNearby) {
			creatureEntity.goalSelector.availableGoals.forEach(prioritizedGoal -> {
				if (prioritizedGoal.getGoal() instanceof AIAvoidExplosionGoal) {
					AIAvoidExplosionGoal aiAvoidExplosionGoal = (AIAvoidExplosionGoal) prioritizedGoal.getGoal();
					aiAvoidExplosionGoal.run(tnt, 8d);
				}
			});
		}
	}
}
