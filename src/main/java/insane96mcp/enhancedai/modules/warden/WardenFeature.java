package insane96mcp.enhancedai.modules.warden;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.WARDEN)
public class WardenFeature extends Feature {

    @Config(description = "Change the max distance from target where warden can use the sonic boom. Vanilla is 15 horizontal and 20 vertical.")
    public static Integer sonicBoomRange = 50;

    @Config(description = "Change the the distance at which the darkness effect is applied.")
    public static Integer darknessRange = 40;

    @Config(description = "Change the the distance at which vibrations reach wardens.")
    public static Integer listenRange = 32;

    @Config(description = "If enabled, will make warden have higher step height making it not need to jump.")
    public static Boolean stepUp = true;

	public static EAIData<Integer> SONIC_BOOM_RANGE;
	public static EAIData<Integer> DARKNESS_RANGE;
	public static EAIData<Integer> LISTEN_RANGE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		SONIC_BOOM_RANGE = EAIData.ofInt(this.createDataKey("sonic_boom_range"));
		DARKNESS_RANGE = EAIData.ofInt(this.createDataKey("darkness_range"));
		LISTEN_RANGE = EAIData.ofInt(this.createDataKey("listen_range"));
    }

    @SubscribeEvent
    public void onWardenJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !stepUp
                || !(event.getEntity() instanceof Warden warden))
            return;

        MCUtils.applyModifier(warden, ForgeMod.STEP_HEIGHT_ADDITION.get(), UUID.fromString("4be0baaf-17a5-4bad-af5a-1b1944ed0bf3"), "Enhanced AI Warden Step Height", 0.5d, AttributeModifier.Operation.ADDITION, true);

		SONIC_BOOM_RANGE.applyIfAbsent(warden, sonicBoomRange);
		DARKNESS_RANGE.applyIfAbsent(warden, darknessRange);
		LISTEN_RANGE.applyIfAbsent(warden, listenRange);
    }

    public static double changeSonicBoomRange(Warden warden, double originalRange) {
        if (!Feature.isEnabled(WardenFeature.class))
            return originalRange;
        return SONIC_BOOM_RANGE.get(warden);
    }

    public static double changeDarknessRange(Warden warden, double originalRange) {
        if (!Feature.isEnabled(WardenFeature.class))
            return originalRange;
        return DARKNESS_RANGE.get(warden);
    }

    public static int changeListenRange(int range) {
        if (!Feature.isEnabled(WardenFeature.class))
            return range;
        return listenRange;
    }
}
