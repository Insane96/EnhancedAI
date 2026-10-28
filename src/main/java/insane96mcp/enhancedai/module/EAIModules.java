package insane96mcp.enhancedai.module;

import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;

public class EAIModules {
	public static final String ANIMAL     = "enhancedai:animal";
	public static final String BLAZE      = "enhancedai:blaze";
	public static final String BUGS       = "enhancedai:bugs";
	public static final String CREEPER    = "enhancedai:creeper";
	public static final String DROWNED    = "enhancedai:drowned";
	public static final String ENDERMAN   = "enhancedai:enderman";
	public static final String GHAST      = "enhancedai:ghast";
	public static final String ILLAGER    = "enhancedai:illager";
	public static final String MOBS       = "enhancedai:mobs";
	public static final String SHULKER    = "enhancedai:shulker";
	public static final String SKELETON   = "enhancedai:skeleton";
	public static final String SLIME      = "enhancedai:slime";
	public static final String SNOW_GOLEM = "enhancedai:snow_golem";
	public static final String SPIDER     = "enhancedai:spider";
	public static final String VILLAGER   = "enhancedai:villager";
	public static final String WARDEN     = "enhancedai:warden";
	public static final String WITCH      = "enhancedai:witch";

	public static void init(IEventBus eventBus) {
		create(ANIMAL,     "Animals",     eventBus);
		create(BLAZE,      "Blazes",      eventBus);
		create(BUGS,       "Bugs",        eventBus);
		create(CREEPER,    "Creepers",    eventBus);
		create(DROWNED,    "Drowned",     eventBus);
		create(ENDERMAN,   "Endermen",    eventBus);
		create(GHAST,      "Ghast",       eventBus);
		create(ILLAGER,    "Illagers",    eventBus);
		create(MOBS,       "Mobs",        eventBus);
		create(SHULKER,    "Shulkers",    eventBus);
		create(SKELETON,   "Skeletons",   eventBus);
		create(SLIME,      "Slimes",      eventBus);
		create(SNOW_GOLEM, "Snow Golems", eventBus);
		create(SPIDER,     "Spiders",     eventBus);
		create(VILLAGER,   "Villagers",   eventBus);
		create(WARDEN,     "Warden",      eventBus);
		create(WITCH,      "Witches",     eventBus);
	}

	public static void create(String id, String name, IEventBus eventBus) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.SERVER, eventBus).build();
	}
}