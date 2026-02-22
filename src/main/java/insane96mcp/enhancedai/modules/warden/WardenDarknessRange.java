package insane96mcp.enhancedai.modules.warden;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.Ids.WARDEN, description = "Change the distance at which the darkness effect is applied.")
public class WardenDarknessRange extends Feature {

    @Config(description = "Vanilla is 20")
    public static Integer range = 50;

	public static EAIData<Integer> RANGE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		RANGE = EAIData.ofInt(this.createDataKey("range"));
    }

    @SubscribeEvent
    public void onWardenJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Warden warden))
            return;

		RANGE.applyIfAbsent(warden, range);
    }

    public static double changeRange(Warden warden, double originalRange) {
        if (!Feature.isEnabled(WardenDarknessRange.class))
            return originalRange;
        return RANGE.get(warden);
    }
}
