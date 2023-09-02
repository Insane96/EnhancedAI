package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Anti-Cheese", description = "Prevent players from abusing some game mechanics to stop mobs")
@LoadFeature(module = Modules.Ids.BASE)
public class AntiCheese extends Feature {

    @Config
    @Label(name = "Prevent Boating & Minecarting", description = "If true, 'Enemies' will no longer be able to be Boated and Minecarted.")
    public static Boolean preventBoating = true;

    public AntiCheese(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMobMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !preventBoating
                || !(event.getEntityMounting() instanceof Enemy)
                || !(event.getEntityBeingMounted() instanceof Boat) && !(event.getEntityBeingMounted() instanceof Minecart))
            return;

        event.setCanceled(true);
    }
}