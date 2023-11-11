package insane96mcp.enhancedai.modules.pets.snowgolem;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Snow Golems", description = "Use the enhancedai:change_snow_golems entity type tag to add more snow golems.")
@LoadFeature(module = Modules.Ids.PETS)
public class SnowGolems extends Feature {
    public static final TagKey<EntityType<?>> CHANGE_SNOW_GOLEMS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "change_snow_golems"));
    public static final String SHOOTING_COOLDOWN = EnhancedAI.RESOURCE_PREFIX + "shooting_cooldown";
    private static final String ON_SPAWN_PROCESSED = EnhancedAI.RESOURCE_PREFIX + "snow_golems_on_spawn_processed";
    @Config
    @Label(name = "Damaging Snowballs")
    public static Boolean damagingSnowballs = true;
    @Config
    @Label(name = "Freezing Snowballs")
    public static Boolean freezingSnowballs = true;
    @Config
    @Label(name = "Healing Snowballs", description = "If true, snowballs hitting snow golems will heal them.")
    public static Boolean healingSnowballs = true;
    //TODO Shooting cooldown

    public SnowGolems(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || (!damagingSnowballs && !freezingSnowballs)
                || !(event.getProjectile().getOwner() instanceof SnowGolem snowGolem)
                || !snowGolem.getType().is(CHANGE_SNOW_GOLEMS)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof LivingEntity entityHit)
                || entityHitResult.getEntity() instanceof SnowGolem)
            return;

        if (damagingSnowballs) {
            DamageSource damageSource = snowGolem.damageSources().mobProjectile(event.getProjectile(), snowGolem);
            entityHit.hurt(damageSource, 0.5f);
        }
        if (freezingSnowballs) {
            entityHit.setTicksFrozen(entityHit.getTicksFrozen() + 30);
        }
    }

    @SubscribeEvent
    public void onProjectileImpactSnowGolemEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || !healingSnowballs
                || !(event.getProjectile() instanceof Snowball)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof SnowGolem snowGolemHit)
                || !snowGolemHit.getType().is(CHANGE_SNOW_GOLEMS))
            return;

        snowGolemHit.heal(1f);
    }


    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof SnowGolem snowGolem)
                || !snowGolem.getType().is(CHANGE_SNOW_GOLEMS))
            return;

        CompoundTag persistentData = snowGolem.getPersistentData();
        int shootingCooldown = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_COOLDOWN, 10);

        snowGolem.goalSelector.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof RangedAttackGoal);
        snowGolem.goalSelector.addGoal(1, new EARangedSnowGolemAttackGoal(snowGolem, 1f, 24f).setAttackCooldown(shootingCooldown));

        if (persistentData.contains(ON_SPAWN_PROCESSED))
            return;

        MCUtils.applyModifier(snowGolem, Attributes.ARMOR, UUID.fromString("4be0baaf-17a5-4bad-af5a-1b1944ed0bf3"), "Armor for snow golems", 5, AttributeModifier.Operation.ADDITION, true);
        persistentData.putBoolean(ON_SPAWN_PROCESSED, true);
    }
}