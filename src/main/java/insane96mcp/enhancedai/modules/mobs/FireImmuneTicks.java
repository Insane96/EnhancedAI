package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS)
public class FireImmuneTicks extends Feature {

    @Config(min = 0, max = 128, description = "How many ticks in fire before setting an entity on fire? Vanilla is 1 for all entities except players where it's 20")
    public static Integer fireImmuneTicks = 20;

    public static EAIData<Integer> FIRE_IMMUNE_TICKS;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        FIRE_IMMUNE_TICKS = EAIData.ofInt(this.createDataKey("fire_immune_ticks"));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob))
            return;

        FIRE_IMMUNE_TICKS.applyIfAbsent(mob, fireImmuneTicks);
    }
}
