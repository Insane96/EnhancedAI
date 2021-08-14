package insane96mcp.enhancedai.base;

import insane96mcp.enhancedai.modules.base.BaseModule;
import insane96mcp.enhancedai.modules.creeper.CreeperModule;
import insane96mcp.enhancedai.modules.skeleton.SkeletonModule;

public class Modules {

	public static BaseModule baseModule;
	public static CreeperModule creeperModule;
	public static SkeletonModule skeletonModule;

	public static void init() {
		baseModule = new BaseModule();
		creeperModule = new CreeperModule();
		skeletonModule = new SkeletonModule();
	}

	public static void loadConfig() {
		baseModule.loadConfig();
		creeperModule.loadConfig();
		skeletonModule.loadConfig();
	}
}
