package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EAAvoidEntityGoal<T extends LivingEntity> extends Goal {
	protected final PathfinderMob entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected T avoidTarget;
	protected final float avoidDistance;
	protected final float avoidDistanceNear;
	protected Path path;
	/** Class of entity this behavior seeks to avoid */
	protected final Class<T> classToAvoid;
	protected final Predicate<LivingEntity> avoidTargetSelector;
	protected final Predicate<LivingEntity> predicateOnAvoidEntity;
	private final TargetingConditions builtTargetSelector;

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> classToAvoidIn, float avoidDistance, float avoidDistanceNear, double nearSpeed, double farSpeed) {
		this(entityIn, classToAvoidIn, (livingEntity) -> true, avoidDistance, avoidDistanceNear, nearSpeed, farSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> avoidClass, Predicate<LivingEntity> targetPredicate, float avoidDistance, float avoidDistanceNear, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> p_i48859_9_) {
		this.entity = entityIn;
		this.classToAvoid = avoidClass;
		this.avoidTargetSelector = targetPredicate;
		this.avoidDistance = avoidDistance * avoidDistance;
		this.avoidDistanceNear = avoidDistanceNear * avoidDistanceNear;
		this.nearSpeed = nearSpeedIn;
		this.farSpeed = farSpeedIn;
		this.predicateOnAvoidEntity = p_i48859_9_;
		this.builtTargetSelector = TargetingConditions.forCombat().range(avoidDistance).selector(p_i48859_9_.and(targetPredicate));
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> avoidClass, float avoidDistance, float avoidDistanceNear, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> targetPredicate) {
		this(entityIn, avoidClass, (p_203782_0_) -> true, avoidDistance, avoidDistanceNear, nearSpeedIn, farSpeedIn, targetPredicate);
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		this.avoidTarget = this.entity.level.getNearestEntity(this.classToAvoid, this.builtTargetSelector, this.entity, this.entity.getX(), this.entity.getY(), this.entity.getZ(), this.entity.getBoundingBox().inflate(this.avoidDistance, this.avoidDistance, this.avoidDistance));
		if (this.avoidTarget == null) {
			return false;
		} else {
			Vec3 vector3d = DefaultRandomPos.getPosAway(this.entity, 16, 7, this.avoidTarget.position());
			if (vector3d == null) {
				return false;
			} else if (this.avoidTarget.distanceToSqr(vector3d.x, vector3d.y, vector3d.z) < this.avoidTarget.distanceToSqr(this.entity)) {
				return false;
			} else {
				this.path = this.entity.getNavigation().createPath(vector3d.x, vector3d.y, vector3d.z, 0);
				return this.path != null;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return !this.entity.getNavigation().isDone();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.entity.getNavigation().moveTo(this.path, this.farSpeed);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.avoidTarget = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.entity.distanceToSqr(this.avoidTarget) < this.avoidDistanceNear) {
			this.entity.getNavigation().setSpeedModifier(this.nearSpeed);
		} else {
			this.entity.getNavigation().setSpeedModifier(this.farSpeed);
		}

	}

	public void setAttackWhenRunning(boolean attackWhenRunning) {
		if (attackWhenRunning)
			this.setFlags(EnumSet.noneOf(Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.LOOK));
	}
}