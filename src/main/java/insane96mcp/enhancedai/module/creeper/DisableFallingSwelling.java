package insane96mcp.enhancedai.module.creeper;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.CREEPER, description = "Creepers will no longer ignite when falling. Only entity types in the enhancedai:creeper/disable_falling_swelling tag will be affected by this feature.")
public class DisableFallingSwelling extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("creeper/disable_falling_swelling"));

	public static EAIData<Boolean> DISABLE_FALLING_SWELLING;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		DISABLE_FALLING_SWELLING = EAIData.ofBool(this.createDataKey("disable_falling_swelling"));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		DISABLE_FALLING_SWELLING.applyIfAbsent(creeper, true);
	}
}
