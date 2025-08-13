package insane96mcp.enhancedai.modules.witch;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidTargetGoal;
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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WITCH, description = "Witches flee from the target. Use the enhancedai:witch_flee_target/can_flee entity type tag to add/remove witches that are affected by this feature")
public class WitchFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> WITCH_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch_flee_target/can_flee"));
    @Config(min = 0d, max = 1d, description = "Chance for a Witch to spawn with the ability to avoid the target")
    public static Double avoidTargetChance = 1d;
    @Config(min = 0d, max = 1d, description = "Chance for a Witch to be able to throw potions while running from a target")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, description = "Distance from the target that will make the Witch run away.")
    public static Integer fleeDistanceFar = 13;
    @Config(min = 0d, description = "Distance from the target that counts as near and will make the Witch run away faster.")
    public static Integer fleeDistanceNear = 7;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the Witch avoids the target and it's farther than 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedFar = 1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the Witch avoids the target and it's within 'Flee Distance Near' blocks from him.")
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
			GoalHelper.removeGoal(mob.goalSelector, EAAvoidTargetGoal.class);
			if (avoidTarget)
				mob.goalSelector.addGoal(1, new EAAvoidTargetGoal(pathfinderMob, FLEE_DISTANCE_FAR, FLEE_DISTANCE_NEAR, FLEE_SPEED_FAR, FLEE_SPEED_NEAR));
			ATTACK_WHEN_AVOIDING.changed(mob);
		});
		ATTACK_WHEN_AVOIDING = EAIData.ofBool(this.createDataKey("attack_when_avoiding"), (mob, attackWhenAvoiding) -> GoalHelper.getGoal(mob.goalSelector, EAAvoidTargetGoal.class).ifPresent(goal -> goal.setAttackWhenRunning(attackWhenAvoiding)));
		FLEE_DISTANCE_FAR = EAIData.ofInt(this.createDataKey("flee_distance_far"));
		FLEE_DISTANCE_NEAR = EAIData.ofInt(this.createDataKey("flee_distance_near"));
		FLEE_SPEED_FAR = EAIData.ofDouble(this.createDataKey("flee_speed_far"));
		FLEE_SPEED_NEAR = EAIData.ofDouble(this.createDataKey("flee_speed_near"));
	}

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Witch witch)
                || !witch.getType().is(WITCH_FLEE))
            return;

		AVOID_TARGET.applyIfAbsent(witch, witch.getRandom().nextDouble() < avoidTargetChance);
		ATTACK_WHEN_AVOIDING.applyIfAbsent(witch, witch.getRandom().nextDouble() < attackWhenAvoidingChance);
		FLEE_DISTANCE_FAR.applyIfAbsent(witch, fleeDistanceFar);
		FLEE_DISTANCE_NEAR.applyIfAbsent(witch, fleeDistanceNear);
		FLEE_SPEED_FAR.applyIfAbsent(witch, fleeSpeedFar);
		FLEE_SPEED_NEAR.applyIfAbsent(witch, fleeSpeedNear);
    }
}
