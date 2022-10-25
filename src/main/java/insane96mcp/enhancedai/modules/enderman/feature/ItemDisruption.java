package insane96mcp.enhancedai.modules.enderman.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Item Disruption", description = "NOT YET IMPLEMENTED. Endermen will make the player's item fall from his hands.")
@LoadFeature(module = Modules.Ids.ENDERMAN, enabledByDefault = false)
public class ItemDisruption extends Feature {

    public ItemDisruption(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onHit(LivingHurtEvent event) {
        /*if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getEntity() instanceof EnderMan enderman))
            return;

        if (!player.getMainHandItem().isEmpty()) {
            event.setCanceled(true);
            player.drop(true);
        }*/
    }
}