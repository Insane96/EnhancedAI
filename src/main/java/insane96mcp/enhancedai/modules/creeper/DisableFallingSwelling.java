package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.CREEPER, description = "Creepers will no longer ignite when falling. Only entity types in the enhancedai:creeper/disable_falling_swelling tag will be affected by this feature.")
public class DisableFallingSwelling extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("creeper/disable_falling_swelling"));

	public static EAIData<Boolean> DISABLE_FALLING_SWELLING;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		DISABLE_FALLING_SWELLING = EAIData.ofBool(this.createDataKey("disable_falling_swelling"));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof net.minecraft.world.entity.monster.Creeper creeper)
				|| !creeper.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		DISABLE_FALLING_SWELLING.applyIfAbsent(creeper, true);
	}
}
