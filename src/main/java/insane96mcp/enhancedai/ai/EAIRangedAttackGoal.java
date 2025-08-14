package insane96mcp.enhancedai.ai;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.utils.GoalHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public abstract class EAIRangedAttackGoal<T extends Mob> extends Goal {
	protected final T mob;
	protected final double movementSpeedMult;
	protected EAIData<Integer> attackCooldown;
	protected EAIData<Double> inaccuracy;
	protected final EAIData<Integer> maxAttackDistance;
	protected int attackTime = -1;
	protected int seeTime;
	protected final EAIData<Boolean> canStrafe;
	protected boolean strafingClockwise;
	protected boolean strafingBackwards;
	protected int strafingTime = -1;

	public EAIRangedAttackGoal(T mob, double movementSpeedMult, EAIData<Integer> attackCooldown, EAIData<Double> inaccuracy, EAIData<Integer> attackDistance, EAIData<Boolean> canStrafe) {
		this.mob = mob;
		this.movementSpeedMult = movementSpeedMult;
		this.attackCooldown = attackCooldown;
		this.inaccuracy = inaccuracy;
		this.maxAttackDistance = attackDistance;
		this.canStrafe = canStrafe;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		return this.mob.getTarget() != null;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return this.canUse();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		super.start();
		this.mob.setAggressive(true);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		super.stop();
		this.mob.setAggressive(false);
		this.seeTime = 0;
		this.attackTime = -1;
		this.mob.stopUsingItem();
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		LivingEntity target = this.mob.getTarget();
		if (target == null)
			return;

		double distanceFromTarget = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
		boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);
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
		double maxAttackDistanceSqr = this.maxAttackDistance.get(this.mob);
		maxAttackDistanceSqr *= maxAttackDistanceSqr;
		if (distanceFromTarget > maxAttackDistanceSqr)
			this.mob.getNavigation().moveTo(target, this.movementSpeedMult);
		else {
			if (distanceFromTarget >= 49d && distanceFromTarget <= maxAttackDistanceSqr && this.seeTime >= 20 && this.canStrafe()) {
				++this.strafingTime;
			}
			else {
				this.strafingTime = -1;
			}

			if (this.strafingTime >= 20) {
				if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
					this.strafingClockwise = !this.strafingClockwise;
				}

				if ((double)this.mob.getRandom().nextFloat() < 0.3D) {
					this.strafingBackwards = !this.strafingBackwards;
				}

				this.strafingTime = 0;
			}

			attackTick(target, distanceFromTarget, canSeeTarget);
		}

	}

	protected boolean canStrafe() {
		return this.canStrafe.get(this.mob) && !GoalHelper.isRunning(this.mob.goalSelector, EAIAvoidEntityGoal.class);
	}

	protected abstract void attackTick(LivingEntity target, double distanceFromTarget, boolean canSeeTarget);

	protected abstract void attackEntityWithRangedAttack(T entity, LivingEntity target, int chargeTicks);

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
