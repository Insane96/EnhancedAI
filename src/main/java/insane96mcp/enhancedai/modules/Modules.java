package insane96mcp.enhancedai.modules;

import insane96mcp.enhancedai.modules.animal.AnimalModule;
import insane96mcp.enhancedai.modules.base.BaseModule;
import insane96mcp.enhancedai.modules.creeper.CreeperModule;
import insane96mcp.enhancedai.modules.skeleton.SkeletonModule;
import insane96mcp.enhancedai.modules.spider.SpiderModule;
import insane96mcp.enhancedai.modules.zombie.ZombieModule;

public class Modules {

	public static BaseModule base;
	public static CreeperModule creeper;
	public static SkeletonModule skeleton;
	public static ZombieModule zombie;
	public static SpiderModule spider;
	public static AnimalModule animal;

	public static void init() {
		base = new BaseModule();
		creeper = new CreeperModule();
		skeleton = new SkeletonModule();
		zombie = new ZombieModule();
		spider = new SpiderModule();
		animal = new AnimalModule();
	}

	public static void loadConfig() {
		base.loadConfig();
		creeper.loadConfig();
		skeleton.loadConfig();
		zombie.loadConfig();
		spider.loadConfig();
		animal.loadConfig();
	}
}
