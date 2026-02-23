package insane96mcp.enhancedai.modules.skeleton.shoot;

import insane96mcp.enhancedai.ai.EAIRangedAttackGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.AbstractSkeletonAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EAIRangedBowAttackGoal extends EAIRangedAttackGoal<AbstractSkeleton> {

	protected EAIData<Integer> bowChargeTicks;

	public EAIRangedBowAttackGoal(AbstractSkeleton mob, double moveSpeedAmpIn, EAIData<Integer> attackCooldown, EAIData<Double> inaccuracy, EAIData<Integer> attackDistance, EAIData<Boolean> canStrafe, EAIData<Integer> bowChargeTicks) {
		super(mob, moveSpeedAmpIn, attackCooldown, inaccuracy, attackDistance, canStrafe);
		this.bowChargeTicks = bowChargeTicks;
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		return super.canUse() && this.isBowInMainhand();
	}

	protected boolean isBowInMainhand() {
		return this.mob.isHolding(stack -> stack.getItem() instanceof BowItem);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return super.canContinueToUse() && this.isBowInMainhand();
	}

	@Override
	protected void attackTick(LivingEntity target, double distanceFromTarget, boolean canSeeTarget) {
		int ticksUsingItem = this.mob.getTicksUsingItem();
		if (ticksUsingItem > 12) {
			this.mob.getNavigation().stop();
			this.mob.lookAt(target, 30.0F, 30.0F);
			this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
		}
		else if (this.strafingTime > -1 && this.canStrafe()) {
			double maxAttackDistanceSqr = this.maxAttackDistance.get(this.mob);
			maxAttackDistanceSqr *= maxAttackDistanceSqr;
			if (distanceFromTarget > maxAttackDistanceSqr * 0.9F) {
				this.strafingBackwards = false;
			}
			else if (distanceFromTarget < maxAttackDistanceSqr * 0.8F) {
				this.strafingBackwards = true;
			}

			this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
		}
		this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

		if (this.mob.isUsingItem()) {
			if (!canSeeTarget && this.seeTime < -60) {
				this.mob.stopUsingItem();
			}
			else if (canSeeTarget) {
				if (ticksUsingItem >= getBowChargeTicks(target)) {
					this.mob.stopUsingItem();
					attackEntityWithRangedAttack(this.mob, target, ticksUsingItem);
					this.attackTime = this.attackCooldown.get(this.mob);
				}
			}
		}
		else if (--this.attackTime <= 0 && this.seeTime >= -60) {
			this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item == Items.BOW));
		}
	}

	private int getBowChargeTicks(LivingEntity target) {
		double distanceFromTarget = this.mob.distanceToSqr(target);
		if (distanceFromTarget < 12d * 12d)
			return this.bowChargeTicks.get(this.mob);

		distanceFromTarget -= 12d * 12d;
		return (int) (this.bowChargeTicks.get(this.mob) + (Math.sqrt(distanceFromTarget)));
	}

	protected void attackEntityWithRangedAttack(AbstractSkeleton entity, LivingEntity target, int chargeTicks) {
		ItemStack weapon = entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(entity, item -> item instanceof net.minecraft.world.item.BowItem));
		ItemStack projectileStack = entity.getProjectile(entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(entity, item -> item == Items.BOW)));
		double distance = entity.distanceTo(target);
		double distanceY = target.getY() - entity.getY();
		float f = 1; //distanceFactor / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		AbstractArrow abstractarrowentity;
		abstractarrowentity = ((AbstractSkeletonAccessor)entity).invokeGetArrow(projectileStack, BowItem.getPowerForTime(chargeTicks), weapon);
		if (entity.getMainHandItem().getItem() instanceof BowItem)
			abstractarrowentity = ((BowItem)entity.getMainHandItem().getItem()).customArrow(abstractarrowentity, projectileStack, weapon);
		double dirX = target.getX() - entity.getX();
		double dirZ = target.getZ() - entity.getZ();
		double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
		double yPos = target.getY(0d);
		yPos += target.getEyeHeight() * 0.5;
		if (distanceXZ != 0f)
			yPos += (distanceY / distanceXZ);
		double dirY = yPos - abstractarrowentity.getY();
		abstractarrowentity.shoot(dirX, dirY + distanceXZ * 0.17d, dirZ, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), this.inaccuracy.get(this.mob).floatValue());
		entity.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
		entity.level().addFreshEntity(abstractarrowentity);
	}

}
