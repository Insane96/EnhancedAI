package insane96mcp.enhancedai.modules.spider.entity.projectile;

import insane96mcp.enhancedai.modules.spider.feature.ThrowingWeb;
import insane96mcp.enhancedai.setup.EAEntities;
import insane96mcp.insanelib.util.scheduled.ScheduledTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class ThrownWebEntity extends ThrowableItemProjectile {

	float damage = 0f;

	public ThrownWebEntity(EntityType<? extends ThrownWebEntity> entityEntityType, Level world) {
		super(entityEntityType, world);
	}

	public ThrownWebEntity(Level worldIn, LivingEntity throwerIn) {
		super(EAEntities.THROWN_WEB.get(), throwerIn, worldIn);
	}

	@Override
	protected @NotNull Item getDefaultItem() {
		return Items.COBWEB;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	protected void onHitEntity(@NotNull EntityHitResult result) {
		super.onHitEntity(result);
		if (!result.getEntity().hurt(DamageSource.thrown(this, this.getOwner()).setScalesWithDifficulty(), this.damage))
			return;
		for(int i = 0; i < 32; ++i) {
			this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), result.getEntity().position().x + this.random.nextDouble() - 0.5d, result.getEntity().position().y + this.random.nextDouble() - 0.5d, result.getEntity().position().z + this.random.nextDouble() - 0.5d, 0d, 0D, 0d);
		}
		this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
		if (!(result.getEntity() instanceof LivingEntity entity) || this.level.isClientSide)
			return;

		ThrowingWeb.applySlowness(entity);
	}

	protected void onHitBlock(BlockHitResult result) {
		BlockState blockstate = this.level.getBlockState(result.getBlockPos());
		blockstate.onProjectileHit(this.level, blockstate, result, this);
		BlockPos spawnCobwebAt = result.getBlockPos().offset(result.getDirection().getNormal());
		if (FallingBlock.isFree(this.level.getBlockState(spawnCobwebAt)) /*&& this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)*/) {
			this.level.setBlock(spawnCobwebAt, Blocks.COBWEB.defaultBlockState(), 3);
			ScheduledTasks.schedule(new TemporaryCobwebTask(ThrowingWeb.destroyWebAfter, this.level, spawnCobwebAt));
			for(int i = 0; i < 32; ++i) {
				this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), spawnCobwebAt.getX() + this.random.nextDouble(), spawnCobwebAt.getY() + this.random.nextDouble(), spawnCobwebAt.getZ() + this.random.nextDouble(), 0d, 0D, 0d);
			}
			this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
		}
	}

	protected void onHit(@NotNull HitResult result) {
		super.onHit(result);
		this.discard();
	}
}
