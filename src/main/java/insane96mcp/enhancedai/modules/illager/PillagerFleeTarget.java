package insane96mcp.enhancedai.modules.illager;

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
import net.minecraft.world.entity.monster.Pillager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ILLAGER, description = "Pillagers try to stay away from the target. Use the enhancedai:pillager_flee/can_flee entity type tag to add/remove skeletons that are affected by this feature")
public class PillagerFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> PILLAGER_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("pillager_flee_target/can_flee"));
    @Config(min = 0d, max = 1d, description = "Chance for a Pillager to spawn with the ability to avoid the target")
    public static Double avoidTargetChance = 0.5d;
    @Config(min = 0d, max = 1d, description = "Chance for a Pillager to be able to shoot while running from a target")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, description = "Distance from the target that will make the Pillager run away.")
    public static Integer fleeDistanceFar = 12;
    @Config(min = 0d, description = "Distance from the target that counts as near and will make the Pillager run away faster.")
    public static Integer fleeDistanceNear = 7;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the Pillager avoids the target and it's farther than 'Flee Distance Far' blocks from him.")
    public static Double fleeSpeedFar = 1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the Pillager avoids the target and it's within 'Flee Distance Near' blocks from him.")
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Pillager pillager)
                || !pillager.getType().is(PILLAGER_FLEE))
            return;

        AVOID_TARGET.applyIfAbsent(pillager, pillager.getRandom().nextDouble() < avoidTargetChance);
        ATTACK_WHEN_AVOIDING.applyIfAbsent(pillager, pillager.getRandom().nextDouble() < attackWhenAvoidingChance);
        FLEE_DISTANCE_FAR.applyIfAbsent(pillager, fleeDistanceFar);
        FLEE_DISTANCE_NEAR.applyIfAbsent(pillager, fleeDistanceNear);
        FLEE_SPEED_FAR.applyIfAbsent(pillager, fleeSpeedFar);
        FLEE_SPEED_NEAR.applyIfAbsent(pillager, fleeSpeedNear);
    }
}