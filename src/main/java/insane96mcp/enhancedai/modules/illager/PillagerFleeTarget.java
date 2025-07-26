package insane96mcp.enhancedai.modules.illager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidTargetGoal;
import insane96mcp.enhancedai.modules.Modules;
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
import net.minecraft.world.entity.monster.Pillager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ILLAGER, description = "Pillagers try to stay away from the target. Use the enhancedai:pillager_flee entity type tag to add/remove skeletons that are affected by this feature")
public class PillagerFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> PILLAGER_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("pillager_flee"));
    @Config(min = 0d, max = 1d, description = "Chance for a Skeleton to spawn with the ability to avoid the player")
    public static Double avoidPlayerChance = 0.5d;
    @Config(min = 0d, max = 1d, description = "Chance for a Skeleton to be able to shoot while running from a player")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that counts as near and will make the skeleton run away faster.")
    public static Double fleeDistanceNear = 7d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that will make the skeleton run away.")
    public static Double fleeDistanceFar = 12d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the pillager avoids the player and it's within 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedNear = 1.1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the pillager avoids the player and it's farther than 'Flee Distance Far' blocks from him.")
    public static Double fleeSpeedFar = 1d;

    public PillagerFleeTarget(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Pillager pillager)
                || !pillager.getType().is(PILLAGER_FLEE))
            return;

        CompoundTag persistentData = pillager.getPersistentData();

        boolean avoidTarget = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.AVOID_TARGET, pillager.getRandom().nextDouble() < avoidPlayerChance);
        boolean attackWhenAvoiding = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.ATTACK_WHEN_AVOIDING, pillager.getRandom().nextDouble() < attackWhenAvoidingChance);
        double fleeDistanceFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_FAR, fleeDistanceFar);
        double fleeDistanceNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_NEAR, fleeDistanceNear);
        double fleeSpeedFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_FAR, fleeSpeedFar);
        double fleeSpeedNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_NEAR, fleeSpeedNear);

        if (!avoidTarget)
            return;

        EAAvoidTargetGoal avoidTargetGoal = new EAAvoidTargetGoal(pillager, (float) fleeDistanceFar1, (float) fleeDistanceNear1, fleeSpeedNear1, fleeSpeedFar1);
        avoidTargetGoal.setAttackWhenRunning(attackWhenAvoiding);
        pillager.goalSelector.addGoal(1, avoidTargetGoal);
    }
}