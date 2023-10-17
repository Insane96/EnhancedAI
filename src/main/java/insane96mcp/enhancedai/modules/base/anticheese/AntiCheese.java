package insane96mcp.enhancedai.modules.base.anticheese;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Anti-Cheese", description = "Prevent players from abusing some game mechanics to stop mobs")
@LoadFeature(module = Modules.Ids.BASE)
public class AntiCheese extends Feature {

    @Config
    @Label(name = "Prevent Boating & Minecarting", description = "If true, 'Enemies' will no longer be able to be Boated and Minecarted.")
    public static Boolean preventBoating = false;

    @Config
    @Label(name = "Break trapping vehicles", description = "If true, 'Enemies' will break boats or minecarts if boated or minecarted.")
    public static Boolean antiBoatAndMinecart = true;

    public AntiCheese(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !(event.getEntityMounting() instanceof Enemy)
                || !(event.getEntityBeingMounted() instanceof Boat) && !(event.getEntityBeingMounted() instanceof Minecart))
            return;

        if (preventBoating)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob))
            return;

        if (antiBoatAndMinecart)
            mob.goalSelector.addGoal(1, new BreakVehicleGoal(mob));
    }
}