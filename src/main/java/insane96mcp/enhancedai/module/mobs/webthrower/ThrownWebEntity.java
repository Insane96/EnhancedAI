package insane96mcp.enhancedai.module.mobs.webthrower;

import insane96mcp.enhancedai.setup.EAIEntities;
import insane96mcp.enhancedai.utils.GoalHelper;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
		super(EAIEntities.THROWN_WEB.get(), throwerIn, worldIn);
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
		if (!result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), this.damage))
			return;
		for(int i = 0; i < 32; ++i) {
			this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), result.getEntity().position().x + this.random.nextDouble() - 0.5d, result.getEntity().position().y + this.random.nextDouble() - 0.5d, result.getEntity().position().z + this.random.nextDouble() - 0.5d, 0d, 0D, 0d);
		}
		this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
		if (!(result.getEntity() instanceof LivingEntity entity) || this.level().isClientSide)
			return;

		if (this.getOwner() != null) {
			if (WebThrower.APPLY_SPEED.get(this.getOwner()) && this.getOwner() instanceof Mob mob && mob.getTarget() == result.getEntity()) {
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
				GoalHelper.getGoal(mob.goalSelector, WebThrowGoal.class).ifPresent(WebThrowGoal::onHit);
			}
			WebThrower.applyEffects(this.getOwner(), entity);
			if (WebThrower.PLACE_WEB_ON_ENTITY_HIT.get(this.getOwner()))
				WebThrower.applyWeb(this.getOwner(), entity.blockPosition());
		}
	}

	protected void onHitBlock(BlockHitResult result) {
		BlockState blockstate = this.level().getBlockState(result.getBlockPos());
		blockstate.onProjectileHit(this.level(), blockstate, result, this);
		if (this.getOwner() == null)
			return;
		if (WebThrower.PLACE_WEB_ON_BLOCK_HIT.get(this.getOwner()))
			WebThrower.applyWeb(this.getOwner(), result.getBlockPos().offset(result.getDirection().getNormal()));
	}

	protected void onHit(@NotNull HitResult result) {
		super.onHit(result);
		this.discard();
	}
}
