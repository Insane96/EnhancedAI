package insane96mcp.enhancedai.modules.warden;

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

    @Config(description = "Multiplies max distance from target where warden can use the sonic boom. Vanilla is 15 horizontal and 20 vertical.")
    public static Double sonicBoomRangeMultiplier = 3d;

    @Config(description = "Multiplies the distance at which the darkness effect is applied.")
    public static Double darknessRangeMultiplier = 2d;

    @Config(description = "Multiplies the distance at which vibrations reach wardens.")
    public static Double listenRangeMultiplier = 2d;

    @Config(description = "If enabled, will make warden have higher step height making it not need to jump.")
    public static Boolean stepUp = true;

    public WardenFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onWardenJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !stepUp
                || !(event.getEntity() instanceof Warden warden))
            return;

        MCUtils.applyModifier(warden, ForgeMod.STEP_HEIGHT_ADDITION.get(), UUID.fromString("4be0baaf-17a5-4bad-af5a-1b1944ed0bf3"), "Enhanced AI Warden Step Height", 0.5d, AttributeModifier.Operation.ADDITION, true);
    }

    public static double increaseSonicBoomRange(double range) {
        if (!Feature.isEnabled(WardenFeature.class)
                || sonicBoomRangeMultiplier == 1f)
            return range;
        return range * sonicBoomRangeMultiplier;
    }

    public static double increaseDarknessRange(double range) {
        if (!Feature.isEnabled(WardenFeature.class)
                || darknessRangeMultiplier == 1f)
            return range;
        return range * darknessRangeMultiplier;
    }

    public static int increaseListenRange(int range) {
        if (!Feature.isEnabled(WardenFeature.class)
                || listenRangeMultiplier == 1f)
            return range;
        return (int) (range * listenRangeMultiplier);
    }
}
