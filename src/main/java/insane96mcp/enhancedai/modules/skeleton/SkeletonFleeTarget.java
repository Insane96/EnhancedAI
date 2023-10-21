package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@Label(name = "Skeleton Flee", description = "Skeletons try to stay away from the target.")
@LoadFeature(module = Modules.Ids.SKELETON)
public class SkeletonFleeTarget extends Feature {
    @Config(min = 0d, max = 1d)
    @Label(name = "Avoid Player chance", description = "Chance for a Skeleton to spawn with the ability to avoid the player")
    public static Double avoidPlayerChance = 0.5d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Attack When Avoiding Chance", description = "Chance for a Skeleton to be able to shoot while running from a player")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, max = 32d)
    @Label(name = "Flee Distance Near", description = "Distance from a player that counts as near and will make the skeleton run away faster.")
    public static Double fleeDistanceNear = 8d;
    @Config(min = 0d, max = 32d)
    @Label(name = "Flee Distance Far", description = "Distance from a player that will make the skeleton run away.")
    public static Double fleeDistanceFar = 16d;
    @Config(min = 0d, max = 4d)
    @Label(name = "Flee speed Multiplier Near", description = "Speed multiplier when the skeleton avoids the player and it's within 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedNear = 1.5d;
    @Config(min = 0d, max = 4d)
    @Label(name = "Flee speed Multiplier Far", description = "Speed multiplier when the skeleton avoids the player and it's farther than 'Flee Distance Far' blocks from him.")
    public static Double fleeSpeedFar = 1.25d;
    @Config
    @Label(name = "Entity Blacklist", description = "Entities that will not be affected by this feature")
    public static Blacklist entityBlacklist = new Blacklist(List.of(
            IdTagMatcher.newId("quark:forgotten")
    ), false);

    public SkeletonFleeTarget(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static void onReassessWeaponGoal(AbstractSkeleton skeleton) {
        if (!isEnabled(SkeletonFleeTarget.class)
                || entityBlacklist.isEntityBlackOrNotWhitelist(skeleton)) return;

        CompoundTag persistentData = skeleton.getPersistentData();

        boolean avoidTarget = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.AVOID_TARGET, skeleton.getRandom().nextDouble() < avoidPlayerChance);
        boolean attackWhenAvoiding = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.ATTACK_WHEN_AVOIDING, skeleton.getRandom().nextDouble() < attackWhenAvoidingChance);
        double fleeDistanceFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_FAR, fleeDistanceFar);
        double fleeDistanceNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_NEAR, fleeDistanceNear);
        double fleeSpeedFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_FAR, fleeSpeedFar);
        double fleeSpeedNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_NEAR, fleeSpeedNear);

        if (!avoidTarget)
            return;

        boolean hasAIArrowAttack = false;
        for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
            if (prioritizedGoal.getGoal().equals(skeleton.bowGoal))
                hasAIArrowAttack = true;
        }
        List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof EAAvoidEntityGoal<?>)
                .toList();

        avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
        if (hasAIArrowAttack) {
            EAAvoidEntityGoal<Player> avoidEntityGoal = new EAAvoidEntityGoal<>(skeleton, Player.class, (float) fleeDistanceFar1, (float) fleeDistanceNear1, fleeSpeedNear1, fleeSpeedFar1);
            avoidEntityGoal.setAttackWhenRunning(attackWhenAvoiding);
            skeleton.goalSelector.addGoal(1, avoidEntityGoal);
        }
    }
}