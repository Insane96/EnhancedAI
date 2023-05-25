package insane96mcp.enhancedai.modules.pets.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Snow Golems")
@LoadFeature(module = Modules.Ids.PETS)
public class SnowGolems extends Feature {
    private static final String ON_SPAWN_PROCESSED = EnhancedAI.RESOURCE_PREFIX + "snow_golems_on_spawn_processed";
    @Config
    @Label(name = "Damaging Snowballs")
    public static Boolean damagingSnowballs = true;
    @Config
    @Label(name = "Freezing Snowballs")
    public static Boolean freezingSnowballs = true;

    @Config
    @Label(name = "Entity Blacklist", description = "Entities that will not be affected by this feature.")
    public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

    public SnowGolems(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || (!damagingSnowballs && !freezingSnowballs)
                || !(event.getProjectile().getOwner() instanceof SnowGolem snowGolem)
                || entityBlacklist.isEntityBlackOrNotWhitelist(snowGolem)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof LivingEntity entityHit))
            return;

        if (damagingSnowballs) {
            DamageSource damageSource = snowGolem.damageSources().mobProjectile(event.getProjectile(), snowGolem);
            entityHit.hurt(damageSource, 1f);
        }
        if (freezingSnowballs) {
            entityHit.setTicksFrozen(140);
        }

        //MCUtils.applyModifier(snowGolem, Attributes.MOVEMENT_SPEED, UUID.fromString("8e68a8b7-f83c-43d4-b5cd-2aac98bf8615"), "Armor for Snow Golems", 1d, AttributeModifier.Operation.MULTIPLY_BASE, true);
    }
}