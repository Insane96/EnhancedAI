package insane96mcp.enhancedai.modules.enderman.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Item Disruption", description = "Endermen will make the player's item fall from his hands.")
public class ItemDisruption extends Feature {

    private final ForgeConfigSpec.DoubleValue getOverHereChanceConfig;

    public double getOverHereChance = 0.15d;

    public ItemDisruption(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        this.getOverHereChanceConfig = Config.builder
                .comment("Chance for a enderman to get the Get Over Here AI")
                .defineInRange("Get Over Here Chance", this.getOverHereChance, 0d, 1d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.getOverHereChance = this.getOverHereChanceConfig.get();
    }

    @SubscribeEvent
    public void onHit(LivingHurtEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getEntity() instanceof EnderMan enderman))
            return;

        if (!player.getMainHandItem().isEmpty()) {
            event.setCanceled(true);
            player.drop(true);
        }
    }
}