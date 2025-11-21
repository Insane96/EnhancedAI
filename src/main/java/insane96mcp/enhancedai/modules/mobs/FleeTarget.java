package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAIAvoidTargetGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Make mobs flee from the target. Use the enhancedai:mobs/can_flee entity type tag to add/remove entities that are affected by this feature. Mobs like animals have they're own feature for fleeing the target due to having different conditions.")
public class FleeTarget extends Feature {
	public static final TagKey<EntityType<?>> CAN_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_flee"));
	@Config(min = 0d, max = 1d, description = "Chance for a mob to spawn with the ability to avoid the target")
	public static Double avoidTargetChance = 0.5d;
	@Config(min = 0d, max = 1d, description = "Chance for a mob to be able to attack while running from a target. This only works for ranged mobs")
	public static Double attackWhenAvoidingChance = 0.5d;
	@Config(min = 0d, description = "Distance from the target that will make the mob run away.")
	public static Integer fleeDistanceFar = 13;
	@Config(min = 0d, description = "Distance from the target that counts as near and will make the mob run away faster.")
	public static Integer fleeDistanceNear = 7;
	@Config(min = 0d, max = 4d, description = "Speed multiplier when the mob avoids the target and it's farther than 'Flee Distance Near' blocks from him.")
	public static Double fleeSpeedFar = 1d;
	@Config(min = 0d, max = 4d, description = "Speed multiplier when the mob avoids the target and it's within 'Flee Distance Near' blocks from him.")
	public static Double fleeSpeedNear = 1.1d;

	public static EAIData<Boolean> AVOID_TARGET;
	public static EAIData<Boolean> ATTACK_WHEN_AVOIDING;
	public static EAIData<Integer> FLEE_DISTANCE_FAR;
	public static EAIData<Integer> FLEE_DISTANCE_NEAR;
	public static EAIData<Double> FLEE_SPEED_FAR;
	public static EAIData<Double> FLEE_SPEED_NEAR;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		AVOID_TARGET = EAIData.ofBool(this.createDataKey("avoid_target"), (mob, avoidTarget) -> {
			if (!(mob instanceof PathfinderMob pathfinderMob))
				return;
			GoalHelper.removeGoal(mob.goalSelector, EAIAvoidTargetGoal.class);
			if (avoidTarget)
				mob.goalSelector.addGoal(1, new EAIAvoidTargetGoal(pathfinderMob, FLEE_DISTANCE_FAR, FLEE_DISTANCE_NEAR, FLEE_SPEED_FAR, FLEE_SPEED_NEAR));
			ATTACK_WHEN_AVOIDING.changed(mob);
		});
		ATTACK_WHEN_AVOIDING = EAIData.ofBool(this.createDataKey("attack_when_avoiding"), (mob, attackWhenAvoiding) -> GoalHelper.getGoal(mob.goalSelector, EAIAvoidTargetGoal.class).ifPresent(goal -> goal.setAttackWhenRunning(attackWhenAvoiding)));
		FLEE_DISTANCE_FAR = EAIData.ofInt(this.createDataKey("flee_distance_far"));
		FLEE_DISTANCE_NEAR = EAIData.ofInt(this.createDataKey("flee_distance_near"));
		FLEE_SPEED_FAR = EAIData.ofDouble(this.createDataKey("flee_speed_far"));
		FLEE_SPEED_NEAR = EAIData.ofDouble(this.createDataKey("flee_speed_near"));
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_FLEE))
			return;

		AVOID_TARGET.applyIfAbsent(mob, mob.getRandom().nextDouble() < avoidTargetChance);
		ATTACK_WHEN_AVOIDING.applyIfAbsent(mob, mob.getRandom().nextDouble() < attackWhenAvoidingChance);
		FLEE_DISTANCE_FAR.applyIfAbsent(mob, fleeDistanceFar);
		FLEE_DISTANCE_NEAR.applyIfAbsent(mob, fleeDistanceNear);
		FLEE_SPEED_FAR.applyIfAbsent(mob, fleeSpeedFar);
		FLEE_SPEED_NEAR.applyIfAbsent(mob, fleeSpeedNear);
	}
}