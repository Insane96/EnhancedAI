package insane96mcp.enhancedai.modules.spider.entity.projectile;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.base.feature.BaseFeature;
import insane96mcp.enhancedai.setup.EAEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class ThrownWebEntity extends ProjectileItemEntity {

	float damage = 0f;

	public ThrownWebEntity(EntityType<? extends ThrownWebEntity> entityEntityType, World world) {
		super(entityEntityType, world);
	}

	public ThrownWebEntity(World worldIn, LivingEntity throwerIn) {
		super(EAEntities.THROWN_WEB.get(), throwerIn, worldIn);
	}

	@OnlyIn(Dist.CLIENT)
	public ThrownWebEntity(World worldIn, double x, double y, double z) {
		super(EntityType.ENDER_PEARL, x, y, z, worldIn);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.COBWEB;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	protected void onHitEntity(EntityRayTraceResult result) {
		super.onHitEntity(result);
		if (!result.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), this.damage))
			return;
		for(int i = 0; i < 32; ++i) {
			this.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), result.getEntity().position().x + this.random.nextDouble() - 0.5d, result.getEntity().position().y + this.random.nextDouble() - 0.5d, result.getEntity().position().z + this.random.nextDouble() - 0.5d, 0d, 0D, 0d);
		}
		this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundCategory.HOSTILE, 1.0f, 0.5f);
		if (!(result.getEntity() instanceof LivingEntity) || this.level.isClientSide)
			return;

		LivingEntity entity = (LivingEntity) result.getEntity();
		EffectInstance slowness = entity.getEffect(Effects.MOVEMENT_SLOWDOWN);
		if (slowness == null)
			entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, 0, true, true, true));
		else
			entity.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 100, Math.min(slowness.getAmplifier() + 1, 4), true, true, true));
	}

	protected void onHitBlock(BlockRayTraceResult result) {
		BlockState blockstate = this.level.getBlockState(result.getBlockPos());
		blockstate.onProjectileHit(this.level, blockstate, result, this);
		BlockPos spawnCobwebAt = result.getBlockPos().offset(result.getDirection().getNormal());
		if (this.level.getBlockState(spawnCobwebAt).isAir() && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			this.level.setBlock(spawnCobwebAt, Blocks.COBWEB.defaultBlockState(), 3);
			BaseFeature.scheduleTickTask(new TemporaryCobwebTask(Modules.spider.throwingWeb.destroyWebAfter, this.level, spawnCobwebAt));
			for(int i = 0; i < 32; ++i) {
				this.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), spawnCobwebAt.getX() + this.random.nextDouble(), spawnCobwebAt.getY() + this.random.nextDouble(), spawnCobwebAt.getZ() + this.random.nextDouble(), 0d, 0D, 0d);
			}
			this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundCategory.HOSTILE, 1.0f, 0.5f);
		}
	}

	protected void onHit(RayTraceResult result) {
		super.onHit(result);
		this.remove();
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
