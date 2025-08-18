package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Change how mobs target players. Check the config options below for entity type tags.")
public class Targeting extends JsonFeature {
	public static final TagKey<EntityType<?>> CHANGE_FOLLOW_RANGE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/follow_range_override"));
	public static final TagKey<EntityType<?>> APPLY_XRAY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/apply_xray"));
	public static final TagKey<EntityType<?>> VISITED_NODES_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/visited_nodes_multiplier"));

	@Config(min = 0d, max = 128d, description = "How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting 'Max' to 0 will leave the follow range as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute. Only mobs in the entity type tag `enhancedai:targeting/follow_range_override` will be affected by this override")
	public static MinMax followRangeOverride = new MinMax(32, 48);
	@Config(min = 0d, max = 128d, description = "How far away can the mobs see the player even through walls. Setting 'Max' to 0 will make mobs not able to see through walls. I recommend using mods like Mobs Properties Randomness to have more control over the attribute; the attribute name is 'enhancedai:generic.xray_follow_range'. Only mobs in the entity type tag `enhancedai:targeting/apply_xray` will be affected by this override.")
	public static MinMax xrayRangeOverride = new MinMax(16, 24);
	@Config(description = "Mobs will be able to find better and longer paths to the target the higher this value is. The higher the more performance heavy. Only entity types in the tag `enhancedai:targeting/visited_nodes_multiplier` tag will be affected by this. Vanilla is 1.0")
	public static Double maxVisitedNodesMultiplier = 4d;

	public static ResourceLocation FOLLOW_RANGES_PROCESSED;
	public static EAIData<Double> MAX_VISITED_NODES_MULTIPLIER;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		FOLLOW_RANGES_PROCESSED = this.createDataKey("follow_ranges_processed");
		MAX_VISITED_NODES_MULTIPLIER = EAIData.ofDouble(this.createDataKey("max_visited_nodes_multiplier"),
				(mob, multiplier) -> mob.getNavigation().setMaxVisitedNodesMultiplier(multiplier.floatValue()));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processFollowRanges(mob);
		processMaxTargetingNodes(mob);
	}

	private void processFollowRanges(Mob mob) {
		if (!ModNBTData.get(mob, FOLLOW_RANGES_PROCESSED, Boolean.class)) {
			//noinspection ConstantConditions
			if (mob.getType().is(CHANGE_FOLLOW_RANGE)
					&& followRangeOverride.max != 0d
					&& mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
				MCUtils.setAttributeValue(mob, Attributes.FOLLOW_RANGE, followRangeOverride.getIntRandBetween(mob.getRandom()));
			}

			//noinspection ConstantConditions
			if (mob.getType().is(APPLY_XRAY)
					&& mob.getAttribute(EAIAttributes.XRAY_FOLLOW_RANGE.get()) != null) {
				MCUtils.setAttributeValue(mob, EAIAttributes.XRAY_FOLLOW_RANGE.get(), xrayRangeOverride.getIntRandBetween(mob.getRandom()));
			}
		}
		ModNBTData.put(mob, FOLLOW_RANGES_PROCESSED, true);
	}

	private void processMaxTargetingNodes(Mob mob) {
		if (!mob.getType().is(VISITED_NODES_MULTIPLIER))
			return;
		MAX_VISITED_NODES_MULTIPLIER.applyIfAbsent(mob, maxVisitedNodesMultiplier);
	}
}
