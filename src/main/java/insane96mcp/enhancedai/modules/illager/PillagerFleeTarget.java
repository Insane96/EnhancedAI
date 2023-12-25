package insane96mcp.enhancedai.modules.illager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Pillager Flee", description = "Pillagers try to stay away from the target. Use the enhancedai:pillager_flee entity type tag to add/remove skeletons that are affected by this feature")
@LoadFeature(module = Modules.Ids.ILLAGER)
public class PillagerFleeTarget extends Feature {
    public static final TagKey<EntityType<?>> PILLAGER_FLEE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "pillager_flee"));
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
    public static Double fleeSpeedNear = 1.1d;
    @Config(min = 0d, max = 4d)
    @Label(name = "Flee speed Multiplier Far", description = "Speed multiplier when the skeleton avoids the player and it's farther than 'Flee Distance Far' blocks from him.")
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

        EAAvoidEntityGoal<Player> avoidEntityGoal = new EAAvoidEntityGoal<>(pillager, Player.class, (float) fleeDistanceFar1, (float) fleeDistanceNear1, fleeSpeedNear1, fleeSpeedFar1);
        avoidEntityGoal.setAttackWhenRunning(attackWhenAvoiding);
        pillager.goalSelector.addGoal(1, avoidEntityGoal);
    }
}