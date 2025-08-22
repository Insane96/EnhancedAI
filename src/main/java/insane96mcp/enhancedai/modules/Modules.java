package insane96mcp.enhancedai.modules;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.setup.EAIConfig;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.config.ModConfig;

public class Modules {
	public static Module animal;
	public static Module blaze;
	public static Module creeper;
	public static Module drowned;
	public static Module ghast;
	public static Module shulker;
	public static Module slime;
	public static Module illager;
	public static Module enderman;
	public static Module bugs;
	public static Module skeleton;
	public static Module snowGolem;
	public static Module spider;
	public static Module villager;
	public static Module witch;
	public static Module mobs;
	public static Module warden;

	public static void init() {
		animal = create(Ids.ANIMAL, "Animals");
		blaze = create(Ids.BLAZE, "Blazes");
		creeper = create(Ids.CREEPER, "Creepers");
		drowned = create(Ids.DROWNED, "Drowned");
		enderman = create(Ids.ENDERMAN, "Endermen");
		ghast = create(Ids.GHAST, "Ghast");
		mobs = create(Ids.MOBS, "Mobs");
		illager = create(Ids.ILLAGER, "Illagers");
		shulker = create(Ids.SHULKER, "Shulkers");
		bugs = create(Ids.BUGS, "Bugs");
		skeleton = create(Ids.SKELETON, "Skeletons");
		snowGolem = create(Ids.SNOW_GOLEM, "Snow Golems");
		slime = create(Ids.SLIME, "Slimes");
		spider = create(Ids.SPIDER, "Spiders");
		villager = create(Ids.VILLAGER, "Villagers");
		witch = create(Ids.WITCH, "Witches");
		warden = create(Ids.WARDEN, "Warden");
	}

	public static Module create(String id, String name) {
		return Module.Builder.create(id, name, ModConfig.Type.COMMON, EAIConfig.builder).build();
	}

	@SuppressWarnings("deprecation")
	public static class Ids {
		public static final String ANIMAL = EnhancedAI.RESOURCE_PREFIX + "animal";
		public static final String BLAZE = EnhancedAI.RESOURCE_PREFIX + "blaze";
		public static final String CREEPER = EnhancedAI.RESOURCE_PREFIX + "creeper";
		public static final String DROWNED = EnhancedAI.RESOURCE_PREFIX + "drowned";
		public static final String ENDERMAN = EnhancedAI.RESOURCE_PREFIX + "enderman";
		public static final String GHAST = EnhancedAI.RESOURCE_PREFIX + "ghast";
		public static final String ILLAGER = EnhancedAI.RESOURCE_PREFIX + "illager";
		public static final String MOBS = EnhancedAI.RESOURCE_PREFIX + "mobs";
		public static final String BUGS = EnhancedAI.RESOURCE_PREFIX + "bugs";
		public static final String SKELETON = EnhancedAI.RESOURCE_PREFIX + "skeleton";
		public static final String SPIDER = EnhancedAI.RESOURCE_PREFIX + "spider";
		public static final String SHULKER = EnhancedAI.RESOURCE_PREFIX + "shulker";
		public static final String SLIME = EnhancedAI.RESOURCE_PREFIX + "slime";
		public static final String SNOW_GOLEM = EnhancedAI.RESOURCE_PREFIX + "snow_golem";
		public static final String VILLAGER = EnhancedAI.RESOURCE_PREFIX + "villager";
		public static final String WARDEN = EnhancedAI.RESOURCE_PREFIX + "warden";
		public static final String WITCH = EnhancedAI.RESOURCE_PREFIX + "witch";
	}
}
