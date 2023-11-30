package insane96mcp.enhancedai.modules.skeleton.shoot;

import insane96mcp.enhancedai.ai.EARangedAttackGoal;
import insane96mcp.enhancedai.setup.Reflection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EARangedBowAttackGoal extends EARangedAttackGoal<AbstractSkeleton> {

	protected int bowChargeTicks;

	public EARangedBowAttackGoal(AbstractSkeleton mob, double moveSpeedAmpIn, float maxAttackDistanceIn, boolean canStrafe) {
		super(mob, moveSpeedAmpIn, maxAttackDistanceIn, canStrafe);
	}

	public EARangedBowAttackGoal setBowChargeTicks(int bowChargeTicks) {
		this.bowChargeTicks = bowChargeTicks;
		return this;
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
			if (distanceFromTarget > (double)(this.maxAttackDistance * 0.9F)) {
				this.strafingBackwards = false;
			}
			else if (distanceFromTarget < (double)(this.maxAttackDistance * 0.8F)) {
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
					this.attackTime = getAttackCooldown(target);
				}
			}
		}
		else if (--this.attackTime <= 0 && this.seeTime >= -60) {
			this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item == Items.BOW));
		}
	}

	private int getAttackCooldown(LivingEntity target) {
		return this.attackCooldown;
	}

	private int getBowChargeTicks(LivingEntity target) {
		double distanceFromTarget = this.mob.distanceToSqr(target);
		if (distanceFromTarget < 12d * 12d)
			return this.bowChargeTicks;

		distanceFromTarget -= 12d * 12d;
		return (int) (this.bowChargeTicks + (Math.sqrt(distanceFromTarget)));
	}

	protected void attackEntityWithRangedAttack(AbstractSkeleton entity, LivingEntity target, int chargeTicks) {
		ItemStack itemstack = entity.getProjectile(entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(entity, item -> item == Items.BOW)));
		double distance = entity.distanceTo(target);
		double distanceY = target.getY() - entity.getY();
		float f = 1; //distanceFactor / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		AbstractArrow abstractarrowentity;
		abstractarrowentity = Reflection.AbstractSkeleton_getArrow(entity, itemstack, BowItem.getPowerForTime(chargeTicks));
		if (entity.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem)
			abstractarrowentity = ((net.minecraft.world.item.BowItem)entity.getMainHandItem().getItem()).customArrow(abstractarrowentity);
		double dirX = target.getX() - entity.getX();
		double dirZ = target.getZ() - entity.getZ();
		double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
		double yPos = target.getY(0d);
		yPos += target.getEyeHeight() * 0.5;
		if (distanceXZ != 0f)
			yPos += (distanceY / distanceXZ);
		double dirY = yPos - abstractarrowentity.getY();
		abstractarrowentity.shoot(dirX, dirY + distanceXZ * 0.17d, dirZ, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), this.inaccuracy);
		entity.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
		entity.level().addFreshEntity(abstractarrowentity);
	}

}
