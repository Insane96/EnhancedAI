package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidTargetGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.skeleton.shoot.EARangedBowAttackGoal;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;

@LoadFeature(module = Modules.Ids.SKELETON, description = "Skeletons try to stay away from the target. Use the enhancedai:skeleton_flee_target/can_flee entity type tag to add/remove skeletons that are affected by this feature. This doesn't work if Skeleton Shoot feature is disabled")
public class SkeletonFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> CAN_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("skeleton_flee_target/can_flee"));
    @Config(min = 0d, max = 1d, description = "Chance for a Skeleton to spawn with the ability to avoid the target")
    public static Double avoidTargetChance = 0.5d;
    @Config(min = 0d, max = 1d, description = "Chance for a Skeleton to be able to shoot while running from the target")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, max = 32d, description = "Distance from a target that will make the skeleton run away.")
    public static Integer fleeDistanceFar = 16;
    @Config(min = 0d, max = 32d, description = "Distance from a target that counts as near and will make the skeleton run away faster.")
    public static Integer fleeDistanceNear = 8;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the skeleton avoids the target and it's farther than 'Flee Distance Far' blocks from him.")
    public static Double fleeSpeedFar = 1.1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the skeleton avoids the target and it's within 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedNear = 1.25d;

    public static EAIData<Boolean> AVOID_TARGET;
    public static EAIData<Boolean> ATTACK_WHEN_AVOIDING;
    public static EAIData<Integer> FLEE_DISTANCE_FAR;
    public static EAIData<Integer> FLEE_DISTANCE_NEAR;
    public static EAIData<Double> FLEE_SPEED_FAR;
    public static EAIData<Double> FLEE_SPEED_NEAR;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        AVOID_TARGET = EAIData.ofBool(this.createDataKey("avoid_target"), (mob, avoidTarget) -> {
            if (!(mob instanceof AbstractSkeleton skeleton))
                return;
            GoalHelper.removeGoal(skeleton.goalSelector, EAAvoidTargetGoal.class);
            if (avoidTarget && (GoalHelper.hasGoal(skeleton.goalSelector, skeleton.bowGoal) || GoalHelper.hasGoal(skeleton.goalSelector, EARangedBowAttackGoal.class)))
                skeleton.goalSelector.addGoal(1, new EAAvoidTargetGoal(skeleton, FLEE_DISTANCE_FAR, FLEE_DISTANCE_NEAR, FLEE_SPEED_FAR, FLEE_SPEED_NEAR));
            ATTACK_WHEN_AVOIDING.changed(mob);
        });
        ATTACK_WHEN_AVOIDING = EAIData.ofBool(this.createDataKey("attack_when_avoiding"), (mob, attackWhenAvoiding) -> {
            GoalHelper.getGoal(mob.goalSelector, EAAvoidTargetGoal.class).ifPresent(goal -> goal.setAttackWhenRunning(attackWhenAvoiding));
        });
        FLEE_DISTANCE_FAR = EAIData.ofInt(this.createDataKey("flee_distance_far"));
        FLEE_DISTANCE_NEAR = EAIData.ofInt(this.createDataKey("flee_distance_near"));
        FLEE_SPEED_FAR = EAIData.ofDouble(this.createDataKey("flee_speed_far"));
        FLEE_SPEED_NEAR = EAIData.ofDouble(this.createDataKey("flee_speed_near"));
    }

    public static void onReassessWeaponGoal(AbstractSkeleton skeleton) {
        if (!isEnabled(SkeletonFleeTarget.class)
                || !skeleton.getType().is(CAN_FLEE))
            return;

        AVOID_TARGET.applyIfAbsent(skeleton, skeleton.getRandom().nextDouble() < avoidTargetChance);
        ATTACK_WHEN_AVOIDING.applyIfAbsent(skeleton, skeleton.getRandom().nextDouble() < attackWhenAvoidingChance);
        FLEE_DISTANCE_FAR.applyIfAbsent(skeleton, fleeDistanceFar);
        FLEE_DISTANCE_NEAR.applyIfAbsent(skeleton, fleeDistanceNear);
        FLEE_SPEED_FAR.applyIfAbsent(skeleton, fleeSpeedFar);
        FLEE_SPEED_NEAR.applyIfAbsent(skeleton, fleeSpeedNear);
    }
}