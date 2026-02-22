package insane96mcp.enhancedai.modules;

import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class EAIModules {
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

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		animal = create(Ids.ANIMAL, "Animals", eventBus, builder);
		blaze = create(Ids.BLAZE, "Blazes", eventBus, builder);
		creeper = create(Ids.CREEPER, "Creepers", eventBus, builder);
		drowned = create(Ids.DROWNED, "Drowned", eventBus, builder);
		enderman = create(Ids.ENDERMAN, "Endermen", eventBus, builder);
		ghast = create(Ids.GHAST, "Ghast", eventBus, builder);
		mobs = create(Ids.MOBS, "Mobs", eventBus, builder);
		illager = create(Ids.ILLAGER, "Illagers", eventBus, builder);
		shulker = create(Ids.SHULKER, "Shulkers", eventBus, builder);
		bugs = create(Ids.BUGS, "Bugs", eventBus, builder);
		skeleton = create(Ids.SKELETON, "Skeletons", eventBus, builder);
		snowGolem = create(Ids.SNOW_GOLEM, "Snow Golems", eventBus, builder);
		slime = create(Ids.SLIME, "Slimes", eventBus, builder);
		spider = create(Ids.SPIDER, "Spiders", eventBus, builder);
		villager = create(Ids.VILLAGER, "Villagers", eventBus, builder);
		witch = create(Ids.WITCH, "Witches", eventBus, builder);
		warden = create(Ids.WARDEN, "Warden", eventBus, builder);
	}

	public static Module create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		return Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}

	public static class Ids {
		public static final String ANIMAL = "enhancedai:animal";
		public static final String BLAZE = "enhancedai:blaze";
		public static final String CREEPER = "enhancedai:creeper";
		public static final String DROWNED = "enhancedai:drowned";
		public static final String ENDERMAN = "enhancedai:enderman";
		public static final String GHAST = "enhancedai:ghast";
		public static final String ILLAGER = "enhancedai:illager";
		public static final String MOBS = "enhancedai:mobs";
		public static final String BUGS = "enhancedai:bugs";
		public static final String SKELETON = "enhancedai:skeleton";
		public static final String SPIDER = "enhancedai:spider";
		public static final String SHULKER = "enhancedai:shulker";
		public static final String SLIME = "enhancedai:slime";
		public static final String SNOW_GOLEM = "enhancedai:snow_golem";
		public static final String VILLAGER = "enhancedai:villager";
		public static final String WARDEN = "enhancedai:warden";
		public static final String WITCH = "enhancedai:witch";
	}
}
