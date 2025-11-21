package insane96mcp.enhancedai.modules.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SLIME, description = "Only entity types in `enhancedai:slime/jump_rate` tag are affected by this feature.")
public class SlimeJumpDelay extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("slime/jump_rate"));

    @Config
    public static MinMax jumpDelay = new MinMax(5, 15);

	public static EAIData<Integer> JUMP_DELAY_MIN;
	public static EAIData<Integer> JUMP_DELAY_MAX;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		JUMP_DELAY_MIN = EAIData.ofInt(this.createDataKey("jump_delay_min"));
		JUMP_DELAY_MAX = EAIData.ofInt(this.createDataKey("jump_delay_max"));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Slime slime)
				|| !event.getEntity().getType().is(AFFECTED_ENTITY_TYPES))
			return;

		JUMP_DELAY_MIN.applyIfAbsent(slime, (int) jumpDelay.min);
		JUMP_DELAY_MAX.applyIfAbsent(slime, (int) jumpDelay.max);
	}
}