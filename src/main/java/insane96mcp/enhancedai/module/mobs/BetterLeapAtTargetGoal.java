package insane96mcp.enhancedai.module.mobs;

import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;

@LoadFeature(module = EAIModules.MOBS, description = "Increases the distance of the leap based off the current delta movement.")
public class BetterLeapAtTargetGoal extends Feature {
    @Config(min = 0d, description = "Vanilla is 0.4")
    public static Double distanceSpeedMultiplier = 0.4d;
    @Config(min = 0d, description = "Vanilla is 0.2")
    public static Double deltaMovementSpeedMultiplier = 0.6d;

    public static boolean shouldUseFeature() {
        return Feature.isEnabled(BetterLeapAtTargetGoal.class);
    }
}
