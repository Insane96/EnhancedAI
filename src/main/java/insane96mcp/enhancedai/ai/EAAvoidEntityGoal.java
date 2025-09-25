package insane96mcp.enhancedai.ai;

import insane96mcp.insanelib.data.IdTagMatcher;
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
	protected final PathfinderMob goalOwner;
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

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> classToAvoidIn, IdTagMatcher idTagMatcher, float avoidDistance, float avoidDistanceNear, double nearSpeed, double farSpeed) {
		this(entityIn, classToAvoidIn, idTagMatcher, (livingEntity) -> true, avoidDistance, avoidDistanceNear, nearSpeed, farSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> avoidClass, Predicate<LivingEntity> targetPredicate, float avoidDistance, float avoidDistanceNear, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> p_i48859_9_) {
		this.goalOwner = entityIn;
		this.classToAvoid = avoidClass;
		this.avoidTargetSelector = targetPredicate;
		this.avoidDistance = avoidDistance;
		this.avoidDistanceNear = avoidDistanceNear;
		this.nearSpeed = nearSpeedIn;
		this.farSpeed = farSpeedIn;
		this.predicateOnAvoidEntity = p_i48859_9_;
		this.builtTargetSelector = TargetingConditions.forCombat().range(avoidDistance).selector(p_i48859_9_.and(targetPredicate));
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> avoidClass, IdTagMatcher idTagMatcher, Predicate<LivingEntity> targetPredicate, float avoidDistance, float avoidDistanceNear, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> predicate) {
		this.goalOwner = entityIn;
		this.classToAvoid = avoidClass;
		this.avoidTargetSelector = targetPredicate;
		this.avoidDistance = avoidDistance;
		this.avoidDistanceNear = avoidDistanceNear;
		this.nearSpeed = nearSpeedIn;
		this.farSpeed = farSpeedIn;
		this.predicateOnAvoidEntity = predicate;
		this.builtTargetSelector = TargetingConditions.forCombat().range(avoidDistance).selector(predicate.and(targetPredicate).and(idTagMatcher::matchesEntity));
	}

	public boolean canUse() {
		this.avoidTarget = this.goalOwner.level().getNearestEntity(this.classToAvoid, this.builtTargetSelector, this.goalOwner, this.goalOwner.getX(), this.goalOwner.getY(), this.goalOwner.getZ(), this.goalOwner.getBoundingBox().inflate(this.avoidDistance));
		if (this.avoidTarget == null) {
			return false;
		} else {
			Vec3 vector3d = DefaultRandomPos.getPosAway(this.goalOwner, 16, 7, this.avoidTarget.position());
			if (vector3d == null) {
				return false;
			} else if (this.avoidTarget.distanceToSqr(vector3d.x, vector3d.y, vector3d.z) < this.avoidTarget.distanceToSqr(this.goalOwner)) {
				return false;
			} else {
				this.path = this.goalOwner.getNavigation().createPath(vector3d.x, vector3d.y, vector3d.z, 0);
				return this.path != null;
			}
		}
	}

	public boolean canContinueToUse() {
		return !this.goalOwner.getNavigation().isDone();
	}

	public void start() {
		this.goalOwner.getNavigation().moveTo(this.path, this.farSpeed);
	}

	public void stop() {
		this.avoidTarget = null;
	}

	public void tick() {
		if (this.goalOwner.distanceToSqr(this.avoidTarget) < this.avoidDistanceNear * this.avoidDistanceNear) {
			this.goalOwner.getNavigation().setSpeedModifier(this.nearSpeed);
		} else {
			this.goalOwner.getNavigation().setSpeedModifier(this.farSpeed);
		}

	}

	public void setAttackWhenRunning(boolean attackWhenRunning) {
		if (attackWhenRunning)
			this.setFlags(EnumSet.noneOf(Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.LOOK));
	}
}