package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.spider.entity.projectile.ThrownWebEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EAEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, EnhancedAI.MOD_ID);

	public static final RegistryObject<EntityType<ThrownWebEntity>> THROWN_WEB = ENTITIES.register("thrown_web", () -> EntityType.Builder.<ThrownWebEntity>of(ThrownWebEntity::new, EntityClassification.MISC)
			.sized(0.25f, 0.25f)
			.setTrackingRange(4)
			.setUpdateInterval(10)
			.setShouldReceiveVelocityUpdates(true)
			.build("thrown_web"));
}
