package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.UseTiConStuffGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Tinkers Construct", description = "Makes mobs be able to use TiCon stuff.")
public class TinkersConstruct extends Feature {
    public TinkersConstruct(Module module) {
        super(Config.builder, module);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)) return;

        mob.goalSelector.addGoal(3, new UseTiConStuffGoal(mob));
    }
}
