package insane96mcp.enhancedai.modules.magmacube;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;

@Label(name = "Slimes")
@LoadFeature(module = Modules.Ids.SLIME)
public class Slimes extends Feature {

    @Config(min = 0, max = 16)
    @Label(name = "Max spawn size", description = "Changes the max size a Magma cube can spawn as. Vanilla is max 4 with 3 excluded. Set to 0 to disable")
    public static Integer maxSpawnSize = 6;

    /*@Config
    @Label(name = "Faster swimming")
    public static Boolean fasterSwimming = true;*/

    @Config(min = 0d, max = 5d)
    @Label(name = "Jump delay multiplier")
    public static Double jumpDelayMultiplier = 0.5d;

    public Slimes(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static boolean shouldOverrideSpawnSize() {
        return isEnabled(Slimes.class) && maxSpawnSize > 0;
    }

    public static boolean shouldChangeJumpDelay() {
        return isEnabled(Slimes.class) && jumpDelayMultiplier != 1d;
    }
}