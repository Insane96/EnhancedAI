package insane96mcp.enhancedai.modules.mobs.avoidexplosion;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.List;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Mobs will run away from exploding creepers / TNT. Only entity types in `enhancedai:mobs/can_run_from_explosions` tag will be affected by this feature")
public class AvoidExplosions extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_run_from_explosions"));
	@Config(min = 0d, max = 1d, description = "Chance for a mob to be able to run from explosions.")
	public static Double chance = 0.8d;
	@Config(min = 0d, max = 10d, description = "Speed multiplier when the mob runs from explosions and it's within 7 blocks from him.")
	public static Double runSpeedNear = 1.1d;
	@Config(min = 0d, max = 10d, description = "Speed multiplier when the mob runs from explosions and it's farther than 7 blocks from him.")
	public static Double runSpeedFar = 1.0d;
	@Config(min = 0d, max = 10d, description = "Entities also flee from TnTs")
	public static Boolean fleeTnt = true;

	public static EAIData<Boolean> CAN_RUN_FROM_EXPLOSIONS;
	public static EAIData<Boolean> CAN_RUN_FROM_TNT;
	public static EAIData<Double> FLEE_SPEED_FAR;
	public static EAIData<Double> FLEE_SPEED_NEAR;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		CAN_RUN_FROM_EXPLOSIONS = EAIData.ofBool(this.createDataKey("can_run_from_explosions"), (mob, canRun) -> {
			if (!(mob instanceof PathfinderMob pathfinderMob))
				return;
			GoalHelper.removeGoal(mob.goalSelector, AvoidExplosionGoal.class);
			if (canRun)
				mob.goalSelector.addGoal(1, new AvoidExplosionGoal(pathfinderMob, FLEE_SPEED_FAR, FLEE_SPEED_NEAR));
		});
		CAN_RUN_FROM_TNT = EAIData.ofBool(this.createDataKey("can_run_from_tnt"));
		FLEE_SPEED_FAR = EAIData.ofDouble(this.createDataKey("flee_speed_far"));
		FLEE_SPEED_NEAR = EAIData.ofDouble(this.createDataKey("flee_speed_near"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity()))
			return;

		alertTNT(event.getEntity());
		if (event.getEntity() instanceof PathfinderMob mob && mob.getType().is(AFFECTED_ENTITY_TYPES)) {
			CAN_RUN_FROM_EXPLOSIONS.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
			FLEE_SPEED_FAR.applyIfAbsent(mob, runSpeedFar);
			FLEE_SPEED_NEAR.applyIfAbsent(mob, runSpeedNear);
			CAN_RUN_FROM_TNT.applyIfAbsent(mob, fleeTnt);
		}
	}

	private void alertTNT(Entity entity) {
		if (entity.getType() != EntityType.TNT)
			return;

		List<PathfinderMob> pathfinderMobs = entity.level().getEntitiesOfClass(PathfinderMob.class, entity.getBoundingBox().inflate(8d));
		for (PathfinderMob pathfinderMob : pathfinderMobs) {
			if (!CAN_RUN_FROM_TNT.get(pathfinderMob)
					|| !pathfinderMob.getType().is(AFFECTED_ENTITY_TYPES))
				continue;
			GoalHelper.getGoal(pathfinderMob.goalSelector, AvoidExplosionGoal.class)
					.ifPresent(goal -> goal.runFrom(entity, 8d));
		}
	}
}
