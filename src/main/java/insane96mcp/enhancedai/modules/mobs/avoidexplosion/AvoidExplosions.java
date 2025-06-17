package insane96mcp.enhancedai.modules.mobs.avoidexplosion;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@LoadFeature(module = Modules.Ids.MOBS, description = "Mobs will run away from exploding creepers / TNT. Use the entity type tag enhancedai:can_run_from_explosion to whitelist them")
public class AvoidExplosions extends Feature {
	public static final TagKey<EntityType<?>> CAN_RUN_FROM_EXPLOSION = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_run_from_explosion"));
	@Config(min = 0d, max = 10d, description = "Speed multiplier when the mob runs from explosions and it's within 7 blocks from him.")
	public static Double runSpeedNear = 1.25d;
	@Config(min = 0d, max = 10d, description = "Speed multiplier when the mob runs from explosions and it's farther than 7 blocks from him.")
	public static Double runSpeedFar = 1.1d;
	@Config(min = 0d, max = 10d, description = "Entities also flee from TnTs")
	public static Boolean fleeTnt = false;

	public AvoidExplosions(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !event.getEntity().getType().is(CAN_RUN_FROM_EXPLOSION))
			return;

		addAvoidAI(event);
		alertTNT(event);
	}

	private void addAvoidAI(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof PathfinderMob creatureEntity))
			return;

		creatureEntity.goalSelector.addGoal(1, new AvoidExplosionGoal(creatureEntity, runSpeedNear, runSpeedFar));
	}

	private void alertTNT(EntityJoinLevelEvent event) {
		if (!fleeTnt)
			return;
		if (!(event.getEntity() instanceof PrimedTnt tnt))
			return;

		List<PathfinderMob> creaturesNearby = tnt.level().getEntitiesOfClass(PathfinderMob.class, tnt.getBoundingBox().inflate(8d));
		for (PathfinderMob creatureEntity : creaturesNearby) {
			creatureEntity.goalSelector.availableGoals.forEach(prioritizedGoal -> {
				if (prioritizedGoal.getGoal() instanceof AvoidExplosionGoal avoidExplosionGoal) {
					avoidExplosionGoal.run(tnt, 8d);
				}
			});
		}
	}
}
