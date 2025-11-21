package insane96mcp.enhancedai.modules.mobs.fallingshockwave;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Mobs will jump off the ground to emit a shockwave when falling damaging nearby entities. Only entity types in enhancedai:mobs/shockwave/can_use tag are affected by this feature and entity types in the enhancedai:mobs/shockwave/damage_invulnerable tag will not be damaged by the shockwave.")
public class FallingShockwave extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/shockwave/can_use"));
    public static final TagKey<EntityType<?>> SHOCKWAVE_INVULNERABLE_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/shockwave/damage_invulnerable"));
    ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, EnhancedAI.location("shockwave"));
    @Config(min = 0)
    public static Double jumpStrength = 1.5d;
    @Config(min = 0, description = "In ticks")
    public static Integer jumpCooldown = 300;
    @Config(min = 0, description = "Damage per block of fall distance.")
    public static Double damagePerBlock = 1d;
    @Config(min = 0, description = "Fall distance multiplied by this")
    public static Double knockupStrengthRatio = 0.08d;
    @Config(min = 0)
    public static Double horizontalBaseRange = 2d;
    @Config(min = 0)
    public static Double verticalBaseRange = 1d;
    @Config(min = 0)
    public static Double horizontalRangePerBlock = 0.2d;
    @Config(min = 0)
    public static Double verticalRangePerBlock = 0.1d;

    public static EAIData<Double> JUMP_STRENGTH;
    public static EAIData<Integer> JUMP_COOLDOWN;
    public static EAIData<Double> DAMAGE_PER_BLOCK;
    public static EAIData<Double> KNOCKUP_STRENGTH_RATIO;
    public static EAIData<Double> HORIZONTAL_BASE_RANGE;
    public static EAIData<Double> VERTICAL_BASE_RANGE;
    public static EAIData<Double> HORIZONTAL_RANGE_PER_BLOCK;
    public static EAIData<Double> VERTICAL_RANGE_PER_BLOCK;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        JUMP_STRENGTH = EAIData.ofDouble(this.createDataKey("jump_strength"), (mob, value) -> {
            GoalHelper.removeGoal(mob.goalSelector, FallingShockwaveGoal.class);
            if (value > 0)
                mob.goalSelector.addGoal(1, new FallingShockwaveGoal(mob));
        });
        JUMP_COOLDOWN = EAIData.ofInt(this.createDataKey("jump_cooldown"));
        DAMAGE_PER_BLOCK = EAIData.ofDouble(this.createDataKey("damage_per_block"));
        KNOCKUP_STRENGTH_RATIO = EAIData.ofDouble(this.createDataKey("knockup_strength_ratio"));
        HORIZONTAL_BASE_RANGE = EAIData.ofDouble(this.createDataKey("horizontal_base_range"));
        VERTICAL_BASE_RANGE = EAIData.ofDouble(this.createDataKey("vertical_base_range"));
        HORIZONTAL_RANGE_PER_BLOCK = EAIData.ofDouble(this.createDataKey("horizontal_range_per_block"));
        VERTICAL_RANGE_PER_BLOCK = EAIData.ofDouble(this.createDataKey("vertical_range_per_block"));
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (!this.isEnabled())
            return;

        LivingEntity entity = event.getEntity();
        double damage = DAMAGE_PER_BLOCK.get(entity);
        if (damage <= 0)
            return;
        if (event.getDistance() < 1d)
            return;
        damage = damage * (event.getDistance() - 1);
        double horizontalBaseRange = HORIZONTAL_BASE_RANGE.get(entity);
        double horizontalRangePerBlock = HORIZONTAL_RANGE_PER_BLOCK.get(entity);
        double horizontalRange = horizontalBaseRange + horizontalRangePerBlock * (event.getDistance() - 1);
        double verticalBaseRange = VERTICAL_BASE_RANGE.get(entity);
        double verticalRangePerBlock = VERTICAL_RANGE_PER_BLOCK.get(entity);
        double verticalRange = verticalBaseRange + verticalRangePerBlock * (event.getDistance() - 1);
        Level level = entity.level();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().setMinY(entity.getY()).setMaxY(entity.getY()).inflate(horizontalRange, verticalRange, horizontalRange))) {
            if (entity != target && target.onGround() && !target.getType().is(SHOCKWAVE_INVULNERABLE_ENTITY_TYPES)) {
                target.hurt(entity.damageSources().source(DAMAGE_TYPE, entity), (float) damage);
                target.setDeltaMovement(target.getDeltaMovement().add(0, (event.getDistance() - 1) * KNOCKUP_STRENGTH_RATIO.get(entity), 0));
                target.hurtMarked = true;
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), (int) (50 * horizontalRange * verticalRange), horizontalRange, verticalRange, horizontalRange, 0);
            serverLevel.playSound(null, entity, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 2f, 0.5f);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        JUMP_STRENGTH.applyIfAbsent(mob, jumpStrength);
        JUMP_COOLDOWN.applyIfAbsent(mob, jumpCooldown);
        DAMAGE_PER_BLOCK.applyIfAbsent(mob, damagePerBlock);
        KNOCKUP_STRENGTH_RATIO.applyIfAbsent(mob, knockupStrengthRatio);
        HORIZONTAL_BASE_RANGE.applyIfAbsent(mob, horizontalBaseRange);
        VERTICAL_BASE_RANGE.applyIfAbsent(mob, verticalBaseRange);
        HORIZONTAL_RANGE_PER_BLOCK.applyIfAbsent(mob, horizontalRangePerBlock);
        VERTICAL_RANGE_PER_BLOCK.applyIfAbsent(mob, verticalRangePerBlock);
    }
}