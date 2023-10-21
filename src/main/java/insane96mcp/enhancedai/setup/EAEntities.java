package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.mobs.fisher.FishingHook;
import insane96mcp.enhancedai.modules.spider.webber.ThrownWebEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EAEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EnhancedAI.MOD_ID);

	public static final RegistryObject<EntityType<ThrownWebEntity>> THROWN_WEB = ENTITIES.register("thrown_web", () -> EntityType.Builder.<ThrownWebEntity>of(ThrownWebEntity::new, MobCategory.MISC)
			.sized(0.25f, 0.25f)
			.setTrackingRange(4)
			.setUpdateInterval(10)
			.setShouldReceiveVelocityUpdates(true)
			.build("thrown_web"));

	public static final RegistryObject<EntityType<FishingHook>> FISHING_HOOK = ENTITIES.register("fishing_hook", () -> EntityType.Builder.<FishingHook>of(FishingHook::new, MobCategory.MISC)
			.noSave()
			.noSummon()
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(5)
			.build("fishing_hook"));
}
