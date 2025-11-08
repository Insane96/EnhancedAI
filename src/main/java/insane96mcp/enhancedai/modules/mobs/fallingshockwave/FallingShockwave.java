package insane96mcp.enhancedai.modules.mobs.fallingshockwave;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
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
    @Config(min = 0)
    public static Double baseRange = 2d;
    @Config(min = 0)
    public static Double rangePerBlock = 0.2d;

    public static EAIData<Double> JUMP_STRENGTH;
    public static EAIData<Integer> JUMP_COOLDOWN;
    public static EAIData<Double> DAMAGE_PER_BLOCK;
    public static EAIData<Double> BASE_RANGE;
    public static EAIData<Double> RANGE_PER_BLOCK;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        JUMP_STRENGTH = EAIData.ofDouble(this.createDataKey("jump_strength"));
        JUMP_COOLDOWN = EAIData.ofInt(this.createDataKey("jump_cooldown"));
        DAMAGE_PER_BLOCK = EAIData.ofDouble(this.createDataKey("damage_per_block"));
        BASE_RANGE = EAIData.ofDouble(this.createDataKey("base_range"));
        RANGE_PER_BLOCK = EAIData.ofDouble(this.createDataKey("range_per_block"));
    }

    @SubscribeEvent
	public void onProjectileImpactSnowGolemEvent(LivingFallEvent event) {
		if (!this.isEnabled())
			return;

        LivingEntity entity = event.getEntity();
        double damage = DAMAGE_PER_BLOCK.get(entity);
        if (damage <= 0)
            return;
        if (event.getDistance() < 1d)
            return;
        damage = damage * (event.getDistance() - 1);
        double baseRange = BASE_RANGE.get(entity);
        double rangePerBlock = RANGE_PER_BLOCK.get(entity);
        double range = baseRange + rangePerBlock * (event.getDistance() - 1);
        Level level = entity.level();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().setMinY(entity.getY()).setMaxY(entity.getY()).inflate(range, 1.5D, range))) {
            if (entity != target && target.onGround() && !target.getType().is(SHOCKWAVE_INVULNERABLE_ENTITY_TYPES)) {
                target.hurt(entity.damageSources().source(DAMAGE_TYPE, entity), (float) damage);
                target.setDeltaMovement(target.getDeltaMovement().add(0, 0.5, 0));
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), (int) (50 * range), range, 1.5, range, 0);
            serverLevel.playSound(null, entity, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 2f, 0.5f);
        }
	}

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        JUMP_STRENGTH.applyIfAbsent(mob, jumpStrength);
        JUMP_COOLDOWN.applyIfAbsent(mob, jumpCooldown);
        DAMAGE_PER_BLOCK.applyIfAbsent(mob, damagePerBlock);
        BASE_RANGE.applyIfAbsent(mob, baseRange);
        RANGE_PER_BLOCK.applyIfAbsent(mob, rangePerBlock);
        mob.goalSelector.addGoal(1, new FallingShockwaveGoal(mob));
    }
}