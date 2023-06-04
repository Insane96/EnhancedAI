package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.base.ai.AvoidExplosionGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.List;

@Label(name = "Avoid Explosions", description = "Mobs will run away from exploding creepers / TNT")
@LoadFeature(module = Modules.Ids.BASE)
public class AvoidExplosions extends Feature {
	@Config(min = 0d, max = 10d)
	@Label(name = "Flee speed Multiplier Near", description = "Speed multiplier when the mob runs from explosions and it's within 7 blocks from him.")
	public static Double runSpeedNear = 1.4d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Flee speed Multiplier Far", description = "Speed multiplier when the mob runs from explosions and it's farther than 7 blocks from him.")
	public static Double runSpeedFar = 1.2d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Flee TNT", description = "Entities also flee from TnTs")
	public static Boolean fleeTnt = false;
	@Config(min = 0d, max = 10d)
	@Label(name = "Entity Blacklist", description = "Entities that shouldn't be affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public AvoidExplosions(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled())
			return;

		addAvoidAI(event);
		alertTNT(event);
	}

	private void addAvoidAI(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof PathfinderMob creatureEntity)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(creatureEntity))
			return;

		creatureEntity.goalSelector.addGoal(1, new AvoidExplosionGoal(creatureEntity, runSpeedNear, runSpeedFar));
	}

	private void alertTNT(EntityJoinLevelEvent event) {
		if (!fleeTnt)
			return;
		if (!(event.getEntity() instanceof PrimedTnt tnt))
			return;

		List<PathfinderMob> creaturesNearby = tnt.level.getEntitiesOfClass(PathfinderMob.class, tnt.getBoundingBox().inflate(8d));
		for (PathfinderMob creatureEntity : creaturesNearby) {
			creatureEntity.goalSelector.availableGoals.forEach(prioritizedGoal -> {
				if (prioritizedGoal.getGoal() instanceof AvoidExplosionGoal avoidExplosionGoal) {
					avoidExplosionGoal.run(tnt, 8d);
				}
			});
		}
	}
}
