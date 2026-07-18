package insane96mcp.enhancedai.module.creeper;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = EAIModules.CREEPER, name = "Tnt like creepers", description = "Creepers will ignite when they take explosion damage. Only entity types in the enhancedai:creeper/tnt_like tag will be affected by this feature.")
public class TNTLike extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("creeper/tnt_like"));

	@Config(min = 0, max = 1d)
	public static Double chance = 0.25d;

	public static EAIData<Boolean> DATA;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		DATA = EAIData.ofBool(this.createDataKey("tnt_like"));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		DATA.applyIfAbsent(creeper, creeper.getRandom().nextDouble() < chance);
	}

	@SubscribeEvent
	public void livingDamageEvent(LivingDamageEvent.Pre event) {
		if (!this.isEnabled()
				|| !event.getSource().is(DamageTypeTags.IS_EXPLOSION)
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !DATA.get(creeper))
			return;

		creeper.ignite();
	}

}
