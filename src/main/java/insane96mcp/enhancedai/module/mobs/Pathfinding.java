package insane96mcp.enhancedai.module.mobs;

import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;

@LoadFeature(module = EAIModules.MOBS)
public class Pathfinding extends Feature {

    @Config(description = "If true, mobs will try to pathfind to the standing position of the player, not the current position. Fixes mobs unable to pathfind to players that have no blocks below them (e.g. when crouching over an edge)")
    public static Boolean pathfindToStandingPosition = true;

    @Config
    public static Boolean fixWalkingOverOpenTrapdoors = true;

    @Config(description = "If true, mobs can freely pathfind over rails even when not already walking on one (by default only mobs already standing on a rail can walk over other rails)")
    public static Boolean mobsCanWalkOnRails = true;

    public static boolean shouldPathfindToStandingPosition() {
        return Feature.isEnabled(Pathfinding.class) && pathfindToStandingPosition;
    }

    public static boolean shouldFixWalkingOverOpenTrapdoors() {
        return Feature.isEnabled(Pathfinding.class) && fixWalkingOverOpenTrapdoors;
    }

    public static boolean shouldMobsWalkOnRails() {
        return Feature.isEnabled(Pathfinding.class) && mobsCanWalkOnRails;
    }
}