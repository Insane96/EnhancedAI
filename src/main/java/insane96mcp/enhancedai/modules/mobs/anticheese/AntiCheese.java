package insane96mcp.enhancedai.modules.mobs.anticheese;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Prevent players from abusing some game mechanics to stop mobs. Only mobs in the entity type tag enhancedai:can_use_anti_cheese will be affected by this feature.")
public class AntiCheese extends Feature {
    public static final TagKey<EntityType<?>> CAN_USE_ANTI_CHEESE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_use_anti_cheese"));

    @Config(description = "If true, 'Enemies' will no longer be able to be Boated and Minecarted.")
    public static Boolean preventBoating = false;

    @Config(description = "If true, 'Enemies' will break boats or minecarts if boated or minecarted.")
    public static Boolean breakTrappingVehicles = true;

    public AntiCheese(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !(event.getEntityMounting() instanceof Enemy)
                || !(event.getEntityBeingMounted() instanceof Boat) && !(event.getEntityBeingMounted() instanceof Minecart)
                || !event.getEntityMounting().getType().is(CAN_USE_ANTI_CHEESE))
            return;

        if (preventBoating)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_USE_ANTI_CHEESE))
            return;

        if (breakTrappingVehicles)
            mob.goalSelector.addGoal(1, new BreakVehicleGoal(mob));
    }
}