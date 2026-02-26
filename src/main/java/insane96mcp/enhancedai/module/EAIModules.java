package insane96mcp.enhancedai.module;

import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

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

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		create(ANIMAL,     "Animals",     eventBus, builder);
		create(BLAZE,      "Blazes",      eventBus, builder);
		create(BUGS,       "Bugs",        eventBus, builder);
		create(CREEPER,    "Creepers",    eventBus, builder);
		create(DROWNED,    "Drowned",     eventBus, builder);
		create(ENDERMAN,   "Endermen",    eventBus, builder);
		create(GHAST,      "Ghast",       eventBus, builder);
		create(ILLAGER,    "Illagers",    eventBus, builder);
		create(MOBS,       "Mobs",        eventBus, builder);
		create(SHULKER,    "Shulkers",    eventBus, builder);
		create(SKELETON,   "Skeletons",   eventBus, builder);
		create(SLIME,      "Slimes",      eventBus, builder);
		create(SNOW_GOLEM, "Snow Golems", eventBus, builder);
		create(SPIDER,     "Spiders",     eventBus, builder);
		create(VILLAGER,   "Villagers",   eventBus, builder);
		create(WARDEN,     "Warden",      eventBus, builder);
		create(WITCH,      "Witches",     eventBus, builder);
	}

	public static void create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}
}