package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.mobs.fisher.EAIFishingHook;
import insane96mcp.enhancedai.module.mobs.webthrower.ThrownWebEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EAIEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, EnhancedAI.MOD_ID);

	public static final DeferredHolder<EntityType<?>, EntityType<ThrownWebEntity>> THROWN_WEB = ENTITIES.register("thrown_web", () -> EntityType.Builder.<ThrownWebEntity>of(ThrownWebEntity::new, MobCategory.MISC)
			.sized(0.25f, 0.25f)
			.setTrackingRange(4)
			.setUpdateInterval(10)
			.setShouldReceiveVelocityUpdates(true)
			.build("thrown_web"));

	public static final DeferredHolder<EntityType<?>, EntityType<EAIFishingHook>> FISHING_HOOK = ENTITIES.register("fishing_hook", () -> EntityType.Builder.<EAIFishingHook>of(EAIFishingHook::new, MobCategory.MISC)
			.noSave()
			.noSummon()
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(5)
			.build("fishing_hook"));
}
