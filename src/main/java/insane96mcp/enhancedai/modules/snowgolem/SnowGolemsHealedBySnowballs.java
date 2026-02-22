package insane96mcp.enhancedai.modules.snowgolem;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@LoadFeature(module = EAIModules.Ids.SNOW_GOLEM, description = "Snow golems are healed when hit by snowballs. Only entity types in enhancedai:snow_golem/healed_by_snowballs tag are affected by this feature.")
public class SnowGolemsHealedBySnowballs extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golem/healed_by_snowballs"));
    @Config(min = 0)
	public static Double amount = 1d;

	@SubscribeEvent
	public void onProjectileImpactSnowGolemEvent(ProjectileImpactEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getProjectile() instanceof Snowball)
				|| !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
				|| !(entityHitResult.getEntity() instanceof SnowGolem snowGolemHit))
			return;

		snowGolemHit.heal(amount.floatValue());
	}
}