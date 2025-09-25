package insane96mcp.enhancedai.modules.mobs;

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

@LoadFeature(module = Modules.Ids.MOBS, description = "Mobs will emit a shockwave when falling damaging nearby entities. Only entity types in enhancedai:mobs/shockwave tag are affected by this feature.")
public class FallingShockwave extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/shockwave"));
    ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, EnhancedAI.location("shockwave"));
    @Config(min = 0, description = "The minimum fall distance for the shockwave to be emitted.")
    public static Integer minFallDistance = 4;
    @Config(min = 0, description = "Damage per block of fall distance after the minimum fall distance.")
    public static Double damagePerBlock = 1d;
    @Config(min = 0)
    public static Double baseRange = 3d;
    @Config(min = 0)
    public static Double rangePerBlock = 0.1d;

    public static EAIData<Integer> MIN_FALL_DISTANCE;
    public static EAIData<Double> DAMAGE_PER_BLOCK;
    public static EAIData<Double> BASE_RANGE;
    public static EAIData<Double> RANGE_PER_BLOCK;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        MIN_FALL_DISTANCE = EAIData.ofInt(this.createDataKey("min_fall_distance"));
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
        int minFallDistance = MIN_FALL_DISTANCE.get(entity);
        if (event.getDistance() < minFallDistance)
            return;
        damage = damage * (event.getDistance() - minFallDistance + 1);
        double baseRange = BASE_RANGE.get(entity);
        double rangePerBlock = RANGE_PER_BLOCK.get(entity);
        double range = baseRange + rangePerBlock * (event.getDistance() - minFallDistance + 1);
        Level level = entity.level();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().setMinY(entity.getY()).setMaxY(entity.getY()).inflate(range, 1D, range))) {
            if (entity != target)
                target.hurt(entity.damageSources().source(DAMAGE_TYPE, entity), (float) damage);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 200, range, 0.5, range, 0);
            serverLevel.playSound(null, entity, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 2f, 0.5f);
        }
	}

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        MIN_FALL_DISTANCE.applyIfAbsent(mob, minFallDistance);
        DAMAGE_PER_BLOCK.applyIfAbsent(mob, damagePerBlock);
        BASE_RANGE.applyIfAbsent(mob, baseRange);
        RANGE_PER_BLOCK.applyIfAbsent(mob, rangePerBlock);
    }
}