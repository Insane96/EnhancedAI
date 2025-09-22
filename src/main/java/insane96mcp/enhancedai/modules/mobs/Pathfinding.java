package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;

@LoadFeature(module = Modules.Ids.MOBS)
public class Pathfinding extends Feature {

    @Config(description = "If true, mobs will try to pathfind to the standing position of the player, not the current position. Fixes mobs unable to pathfind to players that have no blocks below them (e.g. when crouching over an edge)")
    public static Boolean pathfindToStandingPosition = true;

    public static boolean shouldPathfindToStandingPosition() {
        return Feature.isEnabled(Pathfinding.class) && pathfindToStandingPosition;
    }
}