package insane96mcp.enhancedai.modules.skeleton.ai;

import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.base.integration.TinkersConstruct;
import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonShoot;
import insane96mcp.enhancedai.setup.Reflection;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EARangedBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
	private final T entity;
	private final double moveSpeedAmp;
	private int attackCooldown;
	private int bowChargeTicks;
	private float inaccuracy;
	private final float maxAttackDistance;
	private int attackTime = -1;
	private int seeTime;
	private final boolean canStrafe;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	public EARangedBowAttackGoal(T mob, double moveSpeedAmpIn, float maxAttackDistanceIn, boolean canStrafe) {
		this.entity = mob;
		this.moveSpeedAmp = moveSpeedAmpIn;
		this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
		this.canStrafe = canStrafe;
		this.setFlags(EnumSet.of(Goal.Flag.LOOK));
	}

	public EARangedBowAttackGoal<T> setAttackCooldown(int attackCooldownIn) {
		this.attackCooldown = attackCooldownIn;
		return this;
	}

	public EARangedBowAttackGoal<T> setBowChargeTicks(int bowChargeTicks) {
		this.bowChargeTicks = bowChargeTicks;
		return this;
	}

	public EARangedBowAttackGoal<T> setInaccuracy(float inaccuracy) {
		this.inaccuracy = inaccuracy;
		return this;
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		return this.entity.getTarget() != null && isBowInMainhand(this.entity);
	}

	public static boolean isBowInMainhand(LivingEntity entity) {
		return entity.isHolding(stack -> stack.is(SkeletonShoot.BOWS));
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return this.canUse() && isBowInMainhand(this.entity);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		super.start();
		this.entity.setAggressive(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		super.stop();
		this.entity.setAggressive(false);
		this.seeTime = 0;
		this.attackTime = -1;
		this.entity.stopUsingItem();
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		LivingEntity target = this.entity.getTarget();
		if (target == null)
			return;

		double distanceFromTarget = this.entity.distanceToSqr(target.getX(), target.getY(), target.getZ());
		boolean canSeeTarget = this.entity.getSensing().hasLineOfSight(target);
		boolean flag1 = this.seeTime > 0;
		if (canSeeTarget != flag1) {
			this.seeTime = 0;
		}

		if (canSeeTarget) {
			++this.seeTime;
		}
		else {
			--this.seeTime;
		}
		if (distanceFromTarget > (double)this.maxAttackDistance)
			this.entity.getNavigation().moveTo(target, this.moveSpeedAmp);
		else {

			if (distanceFromTarget >= 49d && distanceFromTarget <= (double)this.maxAttackDistance && this.seeTime >= 20 && this.canStrafe()) {
				++this.strafingTime;
			}
			else {
				this.strafingTime = -1;
			}

			if (this.strafingTime >= 20) {
				if ((double)this.entity.getRandom().nextFloat() < 0.3D) {
					this.strafingClockwise = !this.strafingClockwise;
				}

				if ((double)this.entity.getRandom().nextFloat() < 0.3D) {
					this.strafingBackwards = !this.strafingBackwards;
				}

				this.strafingTime = 0;
			}

			int i = this.entity.getTicksUsingItem();
			if (i > 12) {
				this.entity.getNavigation().stop();
				this.entity.lookAt(target, 30.0F, 30.0F);
				this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
			}
			else if (this.strafingTime > -1 && this.canStrafe()) {
				if (distanceFromTarget > (double)(this.maxAttackDistance * 0.9F)) {
					this.strafingBackwards = false;
				}
				else if (distanceFromTarget < (double)(this.maxAttackDistance * 0.8F)) {
					this.strafingBackwards = true;
				}

				this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
			}
			this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

			if (this.entity.isUsingItem()) {
				if (!canSeeTarget && this.seeTime < -60) {
					this.entity.stopUsingItem();
				}
				else if (canSeeTarget) {
					if (i >= this.bowChargeTicks) {
						this.entity.stopUsingItem();
						attackEntityWithRangedAttack(this.entity, target, i);
						this.attackTime = this.attackCooldown;
					}
				}
			}
			else if (--this.attackTime <= 0 && this.seeTime >= -60) {
				this.entity.startUsingItem(getWeaponHoldingHand(this.entity, stack -> stack.is(SkeletonShoot.BOWS)));
			}
		}
	}

	public static InteractionHand getWeaponHoldingHand(LivingEntity livingEntity, Predicate<ItemStack> itemPredicate) {
		return itemPredicate.test(livingEntity.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}

	public static ItemStack getWeaponStack(LivingEntity livingEntity, Predicate<ItemStack> itemPredicate) {
		return livingEntity.getItemInHand(itemPredicate.test(livingEntity.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
	}

	private boolean canStrafe() {
		return this.canStrafe && this.entity.goalSelector.getRunningGoals().noneMatch(p -> p.getGoal() instanceof EAAvoidEntityGoal);
	}

	private void attackEntityWithRangedAttack(T entity, LivingEntity target, int chargeTicks) {
		ItemStack itemstack = entity.getProjectile(getWeaponStack(entity, stack -> stack.is(SkeletonShoot.BOWS)));
		double distance = entity.distanceTo(target);
		double distanceY = target.getY() - entity.getY();
		float f = 1; //distanceFactor / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		AbstractArrow shootArrow;
		if (entity instanceof AbstractSkeleton skeleton)
			shootArrow = Reflection.AbstractSkeleton_getArrow(skeleton, itemstack, BowItem.getPowerForTime(chargeTicks));
		else
			shootArrow = ProjectileUtil.getMobArrow(entity, itemstack, f);
		//shootArrow.setBaseDamage(shootArrow.getBaseDamage() * (distanceFactor / 20f));
		if (entity.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem)
			shootArrow = ((net.minecraft.world.item.BowItem)entity.getMainHandItem().getItem()).customArrow(shootArrow);
		double dirX = target.getX() - entity.getX();
		double dirZ = target.getZ() - entity.getZ();
		double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
		double yPos = target.getY(0d);
		yPos += target.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
		double dirY = yPos - shootArrow.getY();
		shootArrow.shoot(dirX, dirY + distanceXZ * 0.17d, dirZ, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), this.inaccuracy);
		if (ModList.get().isLoaded("tconstruct"))
			TinkersConstruct.setProjectileModifiers(getWeaponStack(entity, stack -> stack.is(SkeletonShoot.BOWS)), entity, shootArrow);
		entity.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
		entity.level.addFreshEntity(shootArrow);
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
