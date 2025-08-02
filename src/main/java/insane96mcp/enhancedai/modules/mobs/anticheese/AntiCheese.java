package insane96mcp.enhancedai.modules.mobs.anticheese;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, name = "Anti-Cheese", description = "Prevent players from abusing some game mechanics to stop mobs, like vehicles or 2 block tall holes for endermen.")
public class AntiCheese extends Feature {
    public static final TagKey<EntityType<?>> VEHICLE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("vehicle"));
    public static final TagKey<EntityType<?>> VALID_VEHICLES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("valid_vehicles"));
    public static final TagKey<EntityType<?>> TELEPORT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("teleport"));
    public static final TagKey<EntityType<?>> CANT_BE_TELEPORTED = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("cant_be_teleported"));

    public static ResourceLocation ANTI_CHEESE;

    @Config(min = 0d, max = 1d, description = "Chance for a mob in the enhancedai:anti_cheese/teleport to get the Teleport Anti-Cheese AI, teleporting the target near them after not being able to reach them. Entity types in the tag enhancedai:anti_cheese/cant_be_teleported")
    public static Double teleportAntiCheese = 1d;

    @Config(description = "If true, 'Enemies' will no longer be able to be put in vehicles. Only mobs in the entity type tag enhancedai:anti_cheese/vehicle will be affected by this. Only vehicles in `enhancedai:anti_cheese/valid_vehicles` will be affected by this.")
    public static Boolean preventRidingVehicles = false;

    @Config(description = "If true, 'Enemies' will break vehicles. Only mobs in the entity type tag enhancedai:anti_cheese/vehicle will be affected by this. Only vehicles in `enhancedai:anti_cheese/valid_vehicles` will be affected by this.")
    public static Boolean breakVehicles = true;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        ANTI_CHEESE = this.createDataKey("anti_cheese");
    }

    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !(event.getEntityMounting() instanceof Enemy)
                || !(event.getEntityBeingMounted().getType().is(VALID_VEHICLES))
                || !event.getEntityMounting().getType().is(VEHICLE)
                || !preventRidingVehicles)
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(VEHICLE)
                || !breakVehicles)
            return;

        mob.goalSelector.addGoal(1, new BreakVehicleGoal(mob));
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(TELEPORT))
            return;

        if (!NBTUtils.getBooleanOrPutDefault(mob, ANTI_CHEESE, mob.getRandom().nextDouble() < teleportAntiCheese))
            return;

        TeleportAntiCheeseGoal teleportAntiCheeseGoal = new TeleportAntiCheeseGoal(mob);
        mob.goalSelector.addGoal(1, teleportAntiCheeseGoal);
    }
}