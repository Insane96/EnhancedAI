package insane96mcp.enhancedai.module.warden;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.WARDEN, description = "Change the max distance from target where warden can use the sonic boom. Vanilla is 15 horizontal and 20 vertical.")
public class WardenSonicBoomRange extends Feature {

    @Config(description = "Vanilla is 15 horizontal and 20 vertical")
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
        if (!Feature.isEnabled(WardenSonicBoomRange.class))
            return originalRange;
        return RANGE.get(warden);
    }
}
