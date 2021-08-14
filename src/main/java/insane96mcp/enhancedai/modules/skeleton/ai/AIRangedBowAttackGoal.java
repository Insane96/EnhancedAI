package insane96mcp.enhancedai.modules.skeleton.ai;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import java.util.UUID;

public class AIRangedBowAttackGoal<T extends MonsterEntity & IRangedAttackMob> extends Goal {
	private final T entity;
	private final double moveSpeedAmp;
	private int attackCooldown;
	private final float maxAttackDistance;
	private int attackTime = -1;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	private final UUID RUN_FROM_TARGET_MODIFIER_UUID = UUID.fromString("16c35012-5150-40b5-ae4f-fd738ec3a638");

	public AIRangedBowAttackGoal(T mob, double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn) {
		this.entity = mob;
		this.moveSpeedAmp = moveSpeedAmpIn;
		this.attackCooldown = attackCooldownIn;
		this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
		this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK));
	}

	public void setAttackCooldown(int attackCooldownIn) {
		this.attackCooldown = attackCooldownIn;
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean shouldExecute() {
		return this.entity.getAttackTarget() == null ? false : this.isBowInMainhand();
	}

	protected boolean isBowInMainhand() {
		return this.entity.func_233634_a_(item -> item instanceof BowItem);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return (this.shouldExecute()) && this.isBowInMainhand();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		super.startExecuting();
		this.entity.setAggroed(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		super.resetTask();
		this.entity.setAggroed(false);
		this.seeTime = 0;
		this.attackTime = -1;
		this.entity.resetActiveHand();
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		LivingEntity livingentity = this.entity.getAttackTarget();
		if (livingentity != null) {
			double distanceFromTarget = this.entity.getDistanceSq(livingentity.getPosX(), livingentity.getPosY(), livingentity.getPosZ());
			boolean canSeeTarget = this.entity.getEntitySenses().canSee(livingentity);
			boolean flag1 = this.seeTime > 0;
			if (canSeeTarget != flag1) {
				this.seeTime = 0;
			}

			if (canSeeTarget) {
				++this.seeTime;
			} else {
				--this.seeTime;
			}

			if (!(distanceFromTarget > (double)this.maxAttackDistance) && this.seeTime >= 20) {
				//this.entity.getNavigator().clearPath();
				++this.strafingTime;
			} else {
				//this.entity.getNavigator().tryMoveToEntityLiving(livingentity, this.moveSpeedAmp);
				this.strafingTime = -1;
			}

			if (this.strafingTime >= 20) {
				if ((double)this.entity.getRNG().nextFloat() < 0.3D) {
					this.strafingClockwise = !this.strafingClockwise;
				}

				if ((double)this.entity.getRNG().nextFloat() < 0.3D) {
					this.strafingBackwards = !this.strafingBackwards;
				}

				this.strafingTime = 0;
			}

			int i = this.entity.getItemInUseMaxCount();
			if (i > 12) {
				this.entity.getNavigator().clearPath();
				this.entity.faceEntity(livingentity, 30.0F, 30.0F);
				this.entity.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
			}
			if (this.strafingTime > -1) {
				if (distanceFromTarget > (double)(this.maxAttackDistance * 0.9F)) {
					this.strafingBackwards = false;
				} else if (distanceFromTarget < (double)(this.maxAttackDistance * 0.8F)) {
					this.strafingBackwards = true;
				}

				//this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
			} else {
				//this.entity.getLookController().setLookPositionWithEntity(livingentity, 30.0F, 30.0F);
			}

			if (this.entity.isHandActive()) {
				if (!canSeeTarget && this.seeTime < -60) {
					this.entity.resetActiveHand();
				} else if (canSeeTarget) {
					if (i >= 20) {
						this.entity.resetActiveHand();
						attackEntityWithRangedAttack(this.entity, livingentity, Math.min(20, i));
						this.attackTime = this.attackCooldown;
					}
				}
			} else if (--this.attackTime <= 0 && this.seeTime >= -60) {
				this.entity.setActiveHand(ProjectileHelper.getHandWith(this.entity, Items.BOW));
			}
		}
	}

	private void attackEntityWithRangedAttack(T entity, LivingEntity target, float distanceFactor) {
		ItemStack itemstack = entity.findAmmo(entity.getHeldItem(ProjectileHelper.getHandWith(entity, Items.BOW)));
		double distance = entity.getDistance(target);
		double distanceY = target.getPosY() - entity.getPosY();
		float f = distanceFactor / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		//TODO Reflection
		//AbstractArrowEntity abstractarrowentity = entity.fireArrow(itemstack, distanceFactor);
		//AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity) Reflection.invokeWithReturn(Reflection.FIRE_ARROW, entity, itemstack, distanceFactor);
		AbstractArrowEntity abstractarrowentity = ProjectileHelper.fireArrow(entity, itemstack, f);
		if (entity.getHeldItemMainhand().getItem() instanceof net.minecraft.item.BowItem)
			abstractarrowentity = ((net.minecraft.item.BowItem)entity.getHeldItemMainhand().getItem()).customArrow(abstractarrowentity);
		double d0 = target.getPosX() - entity.getPosX();
		double d2 = target.getPosZ() - entity.getPosZ();
		double distanceXZ = MathHelper.sqrt(d0 * d0 + d2 * d2);
		double yPos = target.getPosYHeight(0d);
		if (distanceY <= 1d || distanceY > distanceXZ)
			yPos += target.getEyeHeight(target.getPose()) * 0.5 + (distanceY / distanceXZ);
		double d1 = yPos - abstractarrowentity.getPosY();
		EnhancedAI.LOGGER.info(yPos + " " + d1 + " " + distanceXZ + " " + distanceY + " " + this.strafingTime + " " + this.strafingBackwards);
		//abstractarrowentity.shoot(d0, d1 + distanceXZ * (double)0.2F, d2, 1.6F, (float)(14 - entity.world.getDifficulty().getId() * 4));
		abstractarrowentity.shoot(d0, d1 + distanceXZ * 0.2d, d2, f * 1.2f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 0);
		//abstractarrowentity.setGlowing(true);
		entity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (entity.getRNG().nextFloat() * 0.4F + 0.8F));
		entity.world.addEntity(abstractarrowentity);
	}
}
