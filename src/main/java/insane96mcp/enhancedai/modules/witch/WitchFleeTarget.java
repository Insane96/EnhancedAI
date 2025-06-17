package insane96mcp.enhancedai.modules.witch;

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
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WITCH, description = "Witches flee from the target.")
public class WitchFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> WITCH_FLEE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch_flee"));
    @Config(min = 0d, max = 1d, description = "Chance for a Witch to spawn with the ability to avoid the player")
    public static Double avoidPlayerChance = 1d;
    @Config(min = 0d, max = 1d, description = "Chance for a Witch to be able to throw potions while running from a player")
    public static Double attackWhenAvoidingChance = 0.5d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that will make the Witch run away.")
    public static Double fleeDistanceFar = 13d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that counts as near and will make the Witch run away faster.")
    public static Double fleeDistanceNear = 7d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the Witch avoids the player and it's farther than 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedFar = 1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the Witch avoids the player and it's within 'Flee Distance Near' blocks from him.")
    public static Double fleeSpeedNear = 1.1d;

    public WitchFleeTarget(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Witch witch)
                || !witch.getType().is(WITCH_FLEE))
            return;

        CompoundTag persistentData = witch.getPersistentData();

        boolean avoidTarget = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.AVOID_TARGET, witch.getRandom().nextDouble() < avoidPlayerChance);
        boolean attackWhenAvoiding = NBTUtils.getBooleanOrPutDefault(persistentData, EATags.Flee.ATTACK_WHEN_AVOIDING, witch.getRandom().nextDouble() < attackWhenAvoidingChance);
        double fleeDistanceFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_FAR, fleeDistanceFar);
        double fleeDistanceNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_DISTANCE_NEAR, fleeDistanceNear);
        double fleeSpeedFar1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_FAR, fleeSpeedFar);
        double fleeSpeedNear1 = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Flee.FLEE_SPEED_NEAR, fleeSpeedNear);

        if (!avoidTarget)
            return;

        EAAvoidTargetGoal avoidTargetGoal = new EAAvoidTargetGoal(witch, (float) fleeDistanceFar1, (float) fleeDistanceNear1, fleeSpeedNear1, fleeSpeedFar1);
        avoidTargetGoal.setAttackWhenRunning(attackWhenAvoiding);
        witch.goalSelector.addGoal(1, avoidTargetGoal);
    }
}
