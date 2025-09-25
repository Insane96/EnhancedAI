package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.ai.EAAvoidTargetGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.skeleton.shoot.EARangedBowAttackGoal;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;

import java.util.List;

@LoadFeature(module = Modules.Ids.SKELETON, description = "Skeletons try to stay away from the target. Use the enhancedai:skeleton_flee entity type tag to add/remove skeletons that are affected by this feature")
public class SkeletonFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> SKELETON_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("skeleton_flee"));
    @Config(min = 0d, max = 1d, description = "Chance for a Skeleton to spawn with the ability to avoid the player")
    public static Double avoidPlayerChance = 0.5d;
    @Config(min = 0d, max = 1d, description = "Chance for a Skeleton to be able to shoot while running from a player")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that counts as near and will make the skeleton run away faster.")
    public static Double fleeDistanceNear = 8d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that will make the skeleton run away.")
    public static Double fleeDistanceFar = 16d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the skeleton avoids the player and it's within 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedNear = 1.25d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the skeleton avoids the player and it's farther than 'Flee Distance Far' blocks from him.")
    public static Double fleeSpeedFar = 1.1d;

    public SkeletonFleeTarget(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static void onReassessWeaponGoal(AbstractSkeleton skeleton) {
        if (!isEnabled(SkeletonFleeTarget.class)
                || !skeleton.getType().is(SKELETON_FLEE))
            return;

        CompoundTag persistentData = skeleton.getPersistentData();

        boolean avoidTarget = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.AVOID_TARGET, skeleton.getRandom().nextDouble() < avoidPlayerChance);
        if (!avoidTarget)
            return;

        boolean attackWhenAvoiding = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.ATTACK_WHEN_AVOIDING, skeleton.getRandom().nextDouble() < attackWhenAvoidingChance);
        double fleeDistanceFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_FAR, fleeDistanceFar);
        double fleeDistanceNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_NEAR, fleeDistanceNear);
        double fleeSpeedFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_FAR, fleeSpeedFar);
        double fleeSpeedNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_NEAR, fleeSpeedNear);

        boolean hasAIArrowAttack = false;
        for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
            if (prioritizedGoal.getGoal().equals(skeleton.bowGoal) || prioritizedGoal.getGoal() instanceof EARangedBowAttackGoal) {
                hasAIArrowAttack = true;
                break;
            }
        }
        List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof EAAvoidEntityGoal<?>)
                .toList();

        avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
        if (hasAIArrowAttack) {
            EAAvoidTargetGoal avoidTargetGoal = new EAAvoidTargetGoal(skeleton, (float) fleeDistanceFar1, (float) fleeDistanceNear1, fleeSpeedNear1, fleeSpeedFar1);
            avoidTargetGoal.setAttackWhenRunning(attackWhenAvoiding);
            skeleton.goalSelector.addGoal(1, avoidTargetGoal);
        }
    }
}