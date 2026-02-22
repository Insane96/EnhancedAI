package insane96mcp.enhancedai.modules;

import insane96mcp.enhancedai.EnhancedAI;
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

	public static Module create(ResourceLocation id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		return Module.Builder.create(id, name, ModConfig.Type.COMMON, builder, eventBus).build();
	}

	public static class Ids {
		public static final ResourceLocation ANIMAL = EnhancedAI.location("animal");
		public static final ResourceLocation BLAZE = EnhancedAI.location("blaze");
		public static final ResourceLocation CREEPER = EnhancedAI.location("creeper");
		public static final ResourceLocation DROWNED = EnhancedAI.location("drowned");
		public static final ResourceLocation ENDERMAN = EnhancedAI.location("enderman");
		public static final ResourceLocation GHAST = EnhancedAI.location("ghast");
		public static final ResourceLocation ILLAGER = EnhancedAI.location("illager");
		public static final ResourceLocation MOBS = EnhancedAI.location("mobs");
		public static final ResourceLocation BUGS = EnhancedAI.location("bugs");
		public static final ResourceLocation SKELETON = EnhancedAI.location("skeleton");
		public static final ResourceLocation SPIDER = EnhancedAI.location("spider");
		public static final ResourceLocation SHULKER = EnhancedAI.location("shulker");
		public static final ResourceLocation SLIME = EnhancedAI.location("slime");
		public static final ResourceLocation SNOW_GOLEM = EnhancedAI.location("snow_golem");
		public static final ResourceLocation VILLAGER = EnhancedAI.location("villager");
		public static final ResourceLocation WARDEN = EnhancedAI.location("warden");
		public static final ResourceLocation WITCH = EnhancedAI.location("witch");
	}
}
