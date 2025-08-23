package insane96mcp.enhancedai.modules.warden;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WARDEN, description = "Change the distance at which the darkness effect is applied.")
public class WardenDarknessRange extends Feature {

    @Config
    public static Integer range = 50;

	public static EAIData<Integer> RANGE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		RANGE = EAIData.ofInt(this.createDataKey("range"));
    }

    @SubscribeEvent
    public void onWardenJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
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
