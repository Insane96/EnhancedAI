package insane96mcp.enhancedai.modules.snowgolem;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SNOW_GOLEM, description = "Snow golems snowballs deal damage. Only entity types in enhancedai:snow_golem/damaging_snowballs tag are affected by this feature.")
public class SnowGolemsDamagingSnowballs extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golem/damaging_snowballs"));
    @Config(min = 0)
    public static Double damage = 0.5d;

	public static EAIData<Double> DAMAGE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		DAMAGE = EAIData.ofDouble(this.createDataKey("damage"));
    }

    @SubscribeEvent
    public void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || !(event.getProjectile().getOwner() instanceof SnowGolem snowGolem)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof LivingEntity entityHit)
                || entityHitResult.getEntity() instanceof SnowGolem)
            return;

        if (!DAMAGE.has(snowGolem))
			return;

		double damage = DAMAGE.get(snowGolem);
		if (damage <= 0)
			return;
		DamageSource damageSource = snowGolem.damageSources().mobProjectile(event.getProjectile(), snowGolem);
		entityHit.hurt(damageSource, (float) damage);

    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof SnowGolem snowGolem)
				|| !snowGolem.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		DAMAGE.applyIfAbsent(snowGolem, damage);
    }
}