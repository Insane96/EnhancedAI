package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;

@LoadFeature(module = EAIModules.Ids.MOBS)
public class Pathfinding extends Feature {

    @Config(description = "If true, mobs will try to pathfind to the standing position of the player, not the current position. Fixes mobs unable to pathfind to players that have no blocks below them (e.g. when crouching over an edge)")
    public static Boolean pathfindToStandingPosition = true;

    @Config
    public static Boolean fixWalkingOverOpenTrapdoors = true;

    public static boolean shouldPathfindToStandingPosition() {
        return Feature.isEnabled(Pathfinding.class) && pathfindToStandingPosition;
    }

    public static boolean shouldFixWalkingOverOpenTrapdoors() {
        return Feature.isEnabled(Pathfinding.class) && fixWalkingOverOpenTrapdoors;
    }
}