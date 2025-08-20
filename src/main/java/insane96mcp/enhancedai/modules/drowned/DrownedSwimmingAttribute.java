package insane96mcp.enhancedai.modules.drowned;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.DROWNED, description = "Makes drowned swim speed based off swim speed attribute instead of movement speed. Only entity types in the enhancedai:drowned/swimming_attribute tag are affected by this feature.")
public class DrownedSwimmingAttribute extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("drowned/swimming_attribute"));

	final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("ba2adf05-2438-4d1f-8165-89173f0a1eae");

	@Config(min = 0d, max = 4d, description = "Multiplier for the swim speed of Drowned. Note that the swim speed is also affected by the Movement Feature. Set to 0 to disable the multiplier.")
	public static Double swimSpeedMultiplier = 0.3d;

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Drowned drowned)
				|| !drowned.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		((SwimNodeEvaluator) drowned.waterNavigation.getNodeEvaluator()).allowBreaching = true;

		if (swimSpeedMultiplier > 0d) {
			MCUtils.applyModifier(drowned, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Drowned Swim Speed Multiplier", swimSpeedMultiplier - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
		}
	}
}
