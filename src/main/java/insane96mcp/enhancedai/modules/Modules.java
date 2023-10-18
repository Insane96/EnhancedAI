package insane96mcp.enhancedai.modules;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.config.ModConfig;

public class Modules {

	public static Module base;
	public static Module animal;
	public static Module blaze;
	public static Module creeper;
	public static Module drowned;
	public static Module ghast;
	public static Module shulker;
	public static Module slime;
	public static Module golem;
	public static Module pillager;
	public static Module enderman;
	public static Module skeleton;
	public static Module spider;
	public static Module villager;
	public static Module witch;
	public static Module mobs;
	public static Module warden;

	public static void init() {
		base = Module.Builder.create(Ids.BASE, "Base", ModConfig.Type.COMMON, Config.builder).build();
		animal = Module.Builder.create(Ids.ANIMAL, "Animals", ModConfig.Type.COMMON, Config.builder).build();
		blaze = Module.Builder.create(Ids.BLAZE, "Blazes", ModConfig.Type.COMMON, Config.builder).build();
		creeper = Module.Builder.create(Ids.CREEPER, "Creepers", ModConfig.Type.COMMON, Config.builder).build();
		drowned = Module.Builder.create(Ids.DROWNED, "Drowneds", ModConfig.Type.COMMON, Config.builder).build();
		enderman = Module.Builder.create(Ids.ENDERMAN, "Endermen", ModConfig.Type.COMMON, Config.builder).build();
		ghast = Module.Builder.create(Ids.GHAST, "Ghasts", ModConfig.Type.COMMON, Config.builder).build();
		golem = Module.Builder.create(Ids.PETS, "Pets", ModConfig.Type.COMMON, Config.builder).build();
		mobs = Module.Builder.create(Ids.MOBS, "Mobs", ModConfig.Type.COMMON, Config.builder).build();
		pillager = Module.Builder.create(Ids.ILLAGER, "Illagers", ModConfig.Type.COMMON, Config.builder).build();
		shulker = Module.Builder.create(Ids.SHULKER, "Shulkers", ModConfig.Type.COMMON, Config.builder).build();
		skeleton = Module.Builder.create(Ids.SKELETON, "Skeletons", ModConfig.Type.COMMON, Config.builder).build();
		slime = Module.Builder.create(Ids.SLIME, "Slimes", ModConfig.Type.COMMON, Config.builder).build();
		spider = Module.Builder.create(Ids.SPIDER, "Spiders", ModConfig.Type.COMMON, Config.builder).build();
		villager = Module.Builder.create(Ids.VILLAGER, "Villagers", ModConfig.Type.COMMON, Config.builder).build();
		witch = Module.Builder.create(Ids.WITCH, "Witches", ModConfig.Type.COMMON, Config.builder).build();
		warden = Module.Builder.create(Ids.WARDEN, "Warden", ModConfig.Type.COMMON, Config.builder).build();
	}

	public static class Ids {
		public static final String BASE = EnhancedAI.RESOURCE_PREFIX + "base";
		public static final String ANIMAL = EnhancedAI.RESOURCE_PREFIX + "animal";
		public static final String BLAZE = EnhancedAI.RESOURCE_PREFIX + "blaze";
		public static final String CREEPER = EnhancedAI.RESOURCE_PREFIX + "creeper";
		public static final String DROWNED = EnhancedAI.RESOURCE_PREFIX + "drowned";
		public static final String ENDERMAN = EnhancedAI.RESOURCE_PREFIX + "enderman";
		public static final String GHAST = EnhancedAI.RESOURCE_PREFIX + "ghast";
		public static final String ILLAGER = EnhancedAI.RESOURCE_PREFIX + "illager";
		public static final String MOBS = EnhancedAI.RESOURCE_PREFIX + "mobs";
		public static final String PETS = EnhancedAI.RESOURCE_PREFIX + "pets";
		public static final String SKELETON = EnhancedAI.RESOURCE_PREFIX + "skeleton";
		public static final String SPIDER = EnhancedAI.RESOURCE_PREFIX + "spider";
		public static final String SHULKER = EnhancedAI.RESOURCE_PREFIX + "shulker";
		public static final String SLIME = EnhancedAI.RESOURCE_PREFIX + "slime";
		public static final String VILLAGER = EnhancedAI.RESOURCE_PREFIX + "villager";
		public static final String WARDEN = EnhancedAI.RESOURCE_PREFIX + "warden";
		public static final String WITCH = EnhancedAI.RESOURCE_PREFIX + "witch";
	}
}
