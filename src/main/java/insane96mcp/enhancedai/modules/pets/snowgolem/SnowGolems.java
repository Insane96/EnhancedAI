package insane96mcp.enhancedai.modules.pets.snowgolem;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
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

@LoadFeature(module = Modules.Ids.PETS, description = "Use the enhancedai:snow_golems/damaging_snowballs, enhancedai:snow_golems/freezing_snowballs, enhancedai:snow_golems/better_shooting entity type tags to add more snow golems.")
public class SnowGolems extends Feature {
    public static final TagKey<EntityType<?>> DAMAGING_SNOWBALLS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golems/damaging_snowballs"));
    public static final TagKey<EntityType<?>> FREEZING_SNOWBALLS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golems/freezing_snowballs"));
    public static final TagKey<EntityType<?>> BETTER_SHOOTING = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golems/better_shooting"));
    @Config(min = 0)
    public static Double damagingSnowballs = 0.5d;
    @Config(min = 0)
    public static Integer freezingSnowballs = 30;
    @Config(description = "If true, snowballs hitting snow golems will heal them.")
    public static Boolean healingSnowballs = true;
    @Config(min = 1, max = 64, description = "The range from where a snow golem will shoot the target")
    public static MinMax shootingRange = new MinMax(24, 32);
    @Config(min = 0, description = "The ticks cooldown before shooting. Vanilla is random between 20 and 40")
    public static MinMax shootingCooldown = new MinMax(20, 40);
    @Config(min = 0d, max = 30d, description = "How much inaccuracy does the snowball fired by snow golems have. Vanilla is 12.")
    public static MinMax inaccuracy = new MinMax(1, 3);
    @Config(min = 0d, max = 1d, description = "Chance to be able to strafe while shooting. Vanilla golems don't strafe")
    public static Double strafeChance = 0d;
    @Config(min = 0)
    public static Integer bonusArmor = 5;

    public static EAIData<Integer> SHOOTING_RANGE;
    public static EAIData<Double> INACCURACY;
    public static EAIData<Integer> SHOOTING_COOLDOWN;
    public static EAIData<Boolean> STRAFE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        SHOOTING_RANGE = EAIData.ofInt(this.createDataKey("shooting_range"));
        INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
        SHOOTING_COOLDOWN = EAIData.ofInt(this.createDataKey("shooting_cooldown"));
        STRAFE = EAIData.ofBool(this.createDataKey("strafe"));
    }

    @SubscribeEvent
    public void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || (damagingSnowballs == 0f && freezingSnowballs == 0)
                || !(event.getProjectile().getOwner() instanceof SnowGolem snowGolem)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof LivingEntity entityHit)
                || entityHitResult.getEntity() instanceof SnowGolem)
            return;

        if (damagingSnowballs > 0f && snowGolem.getType().is(DAMAGING_SNOWBALLS)) {
            DamageSource damageSource = snowGolem.damageSources().mobProjectile(event.getProjectile(), snowGolem);
            entityHit.hurt(damageSource, damagingSnowballs.floatValue());
        }
        if (freezingSnowballs == 0 && snowGolem.getType().is(FREEZING_SNOWBALLS))
            entityHit.setTicksFrozen(entityHit.getTicksFrozen() + freezingSnowballs);
    }

    @SubscribeEvent
    public void onProjectileImpactSnowGolemEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || !healingSnowballs
                || !(event.getProjectile() instanceof Snowball)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof SnowGolem snowGolemHit))
            return;

        snowGolemHit.heal(1f);
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof SnowGolem snowGolem)
                || !snowGolem.getType().is(BETTER_SHOOTING))
            return;

        snowGolem.goalSelector.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof RangedAttackGoal);
        snowGolem.goalSelector.addGoal(1, new EARangedSnowGolemAttackGoal(snowGolem, 1f, SHOOTING_COOLDOWN, INACCURACY, SHOOTING_RANGE, STRAFE));
        SHOOTING_RANGE.applyIfAbsent(snowGolem, shootingRange.getIntRandBetween(snowGolem.getRandom()));
        INACCURACY.applyIfAbsent(snowGolem, inaccuracy.getRandBetween(snowGolem.getRandom()));
        SHOOTING_COOLDOWN.applyIfAbsent(snowGolem, shootingCooldown.getIntRandBetween(snowGolem.getRandom()));
        STRAFE.applyIfAbsent(snowGolem, snowGolem.getRandom().nextDouble() < strafeChance);

        MCUtils.applyModifier(snowGolem, Attributes.ARMOR, UUID.fromString("4be0baaf-17a5-4bad-af5a-1b1944ed0bf3"), "Armor for snow golems", bonusArmor, AttributeModifier.Operation.ADDITION, true);
    }
}