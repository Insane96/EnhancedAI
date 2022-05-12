package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Anti-Cheese", description = "Prevent players from abusing some game mechanics to stop mobs")
public class AntiCheese extends Feature {

    private final ForgeConfigSpec.ConfigValue<Boolean> preventBoatingConfig;

    public boolean preventBoating = true;

    public AntiCheese(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        this.preventBoatingConfig = Config.builder
                .comment("If true, 'Enemies' will no longer be able to be Boated and Minecarted.")
                .define("Prevent Boating & Minecarting", this.preventBoating);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.preventBoating = this.preventBoatingConfig.get();
    }

    @SubscribeEvent
    public void onMobSpawn(EntityMountEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntityMounting() instanceof Enemy))
            return;

        if (!(event.getEntityBeingMounted() instanceof Boat) && !(event.getEntityBeingMounted() instanceof Minecart))
            return;

        event.setCanceled(true);
    }
}