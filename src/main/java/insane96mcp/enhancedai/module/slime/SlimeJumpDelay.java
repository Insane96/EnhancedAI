package insane96mcp.enhancedai.module.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.SLIME, description = "Only entity types in `enhancedai:slime/jump_rate` tag are affected by this feature.")
public class SlimeJumpDelay extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("slime/jump_rate"));

    @Config
    public static MinMaxConfig jumpDelay = new MinMaxConfig(5, 15);

	public static EAIData<Integer> JUMP_DELAY_MIN;
	public static EAIData<Integer> JUMP_DELAY_MAX;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		JUMP_DELAY_MIN = EAIData.ofInt(this.createDataKey("jump_delay_min"));
		JUMP_DELAY_MAX = EAIData.ofInt(this.createDataKey("jump_delay_max"));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Slime slime)
				|| !event.getEntity().getType().is(AFFECTED_ENTITY_TYPES))
			return;

		JUMP_DELAY_MIN.applyIfAbsent(slime, (int) jumpDelay.min);
		JUMP_DELAY_MAX.applyIfAbsent(slime, (int) jumpDelay.max);
	}
}