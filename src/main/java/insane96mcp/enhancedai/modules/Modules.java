package insane96mcp.enhancedai.modules;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Module;

public class Modules {

	public static Module base;
	public static Module animal;
	public static Module blaze;
	public static Module creeper;
	public static Module drowned;
	public static Module ghast;
	public static Module enderman;
	public static Module skeleton;
	public static Module spider;
	public static Module villager;
	public static Module witch;
	public static Module zombie;

	public static void init() {
		base = Module.Builder.create(Config.builder, Ids.BASE, "Base").build();
		animal = Module.Builder.create(Config.builder, Ids.ANIMAL, "Animals").build();
		blaze = Module.Builder.create(Config.builder, Ids.BLAZE, "Blazes").build();
		creeper = Module.Builder.create(Config.builder, Ids.CREEPER, "Creepers").build();
		drowned = Module.Builder.create(Config.builder, Ids.DROWNED, "Drowneds").build();
		ghast = Module.Builder.create(Config.builder, Ids.GHAST, "Ghasts").build();
		enderman = Module.Builder.create(Config.builder, Ids.ENDERMAN, "Endermen").build();
		skeleton = Module.Builder.create(Config.builder, Ids.SKELETON, "Skeletons").build();
		spider = Module.Builder.create(Config.builder, Ids.SPIDER, "Spiders").build();
		villager = Module.Builder.create(Config.builder, Ids.VILLAGER, "Villagers").build();
		witch = Module.Builder.create(Config.builder, Ids.WITCH, "Witches").build();
		zombie = Module.Builder.create(Config.builder, Ids.ZOMBIE, "Zombies").build();
	}

	public static class Ids {
		public static final String BASE = EnhancedAI.RESOURCE_PREFIX + "base";
		public static final String ANIMAL = EnhancedAI.RESOURCE_PREFIX + "animal";
		public static final String BLAZE = EnhancedAI.RESOURCE_PREFIX + "blaze";
		public static final String CREEPER = EnhancedAI.RESOURCE_PREFIX + "creeper";
		public static final String DROWNED = EnhancedAI.RESOURCE_PREFIX + "drowned";
		public static final String GHAST = EnhancedAI.RESOURCE_PREFIX + "ghast";
		public static final String ENDERMAN = EnhancedAI.RESOURCE_PREFIX + "enderman";
		public static final String SKELETON = EnhancedAI.RESOURCE_PREFIX + "skeleton";
		public static final String SPIDER = EnhancedAI.RESOURCE_PREFIX + "spider";
		public static final String VILLAGER = EnhancedAI.RESOURCE_PREFIX + "villager";
		public static final String WITCH = EnhancedAI.RESOURCE_PREFIX + "witch";
		public static final String ZOMBIE = EnhancedAI.RESOURCE_PREFIX + "zombie";
	}
}
