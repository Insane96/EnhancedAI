package insane96mcp.enhancedai.modules.mobs.anticheese;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
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

@LoadFeature(module = Modules.Ids.MOBS, name = "Anti-Cheese", description = "Prevent players from abusing some game mechanics to stop mobs. Only mobs in the entity type tag enhancedai:can_use_anti_cheese will be affected by this feature. Only veichles in `enhancedai:anti_cheese_veichles` will be affected by this feature.")
public class AntiCheese extends Feature {
    public static final TagKey<EntityType<?>> CAN_USE_ANTI_CHEESE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_use_anti_cheese"));
    public static final TagKey<EntityType<?>> ANTI_CHEESE_VEICHLES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "anti_cheese_vehicles"));

    @Config(description = "If true, 'Enemies' will no longer be able to be put in vehicles.")
    public static Boolean preventRidingVehicles = false;

    @Config(description = "If true, 'Enemies' will break vehicles.")
    public static Boolean breakVehicles = true;

    public AntiCheese(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !(event.getEntityMounting() instanceof Enemy)
                || !(event.getEntityBeingMounted().getType().is(ANTI_CHEESE_VEICHLES))
                || !event.getEntityMounting().getType().is(CAN_USE_ANTI_CHEESE)
                || !preventRidingVehicles)
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_USE_ANTI_CHEESE)
                || !breakVehicles)
            return;

        mob.goalSelector.addGoal(1, new BreakVehicleGoal(mob));
    }
}