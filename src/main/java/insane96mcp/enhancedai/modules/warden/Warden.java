package insane96mcp.enhancedai.modules.warden;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;

@Label(name = "Warden Attacking")
@LoadFeature(module = Modules.Ids.WARDEN)
public class Warden extends Feature {

    @Config
    @Label(name = "Sonic Boom range multiplier", description = "Multiplies max distance from target where warden can use the sonic boom. Vanilla is 15 horizontal and 20 vertical.")
    public static Double sonicBoomRangeMultiplier = 3d;

    @Config
    @Label(name = "Darkness range multiplier", description = "Multiplies the distance at which the darkness effect is applied.")
    public static Double darknessRangeMultiplier = 2d;

    public Warden(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static double increaseSonicBoomRange(double range) {
        if (!Feature.isEnabled(Warden.class)
                || sonicBoomRangeMultiplier == 1f)
            return range;
        return range * sonicBoomRangeMultiplier;
    }

    public static double increaseDarknessRange(double range) {
        if (!Feature.isEnabled(Warden.class)
                || darknessRangeMultiplier == 1f)
            return range;
        return range * darknessRangeMultiplier;
    }
}
