package insane96mcp.enhancedai.ai;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class EAAvoidEntityGoal<T extends LivingEntity> extends Goal {
	protected final PathfinderMob goalOwner;
	private final EAIData<Double> farSpeed;
	private final EAIData<Double> nearSpeed;
	protected T avoidTarget;
	protected final EAIData<Integer> avoidDistance;
	protected final EAIData<Integer> avoidDistanceNear;
	protected Path path;
	/** Class of entity this behavior seeks to avoid */
	protected final Class<T> classToAvoid;
	protected final Predicate<LivingEntity> avoidTargetSelector;
	protected final Predicate<LivingEntity> predicateOnAvoidEntity;
	private final TargetingConditions builtTargetSelector;

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> classToAvoidIn, EAIData<Integer> avoidDistance, EAIData<Integer> avoidDistanceNear, EAIData<Double> farSpeed, EAIData<Double> nearSpeed) {
		this(entityIn, classToAvoidIn, (livingEntity) -> true, avoidDistance, avoidDistanceNear, farSpeed, nearSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> classToAvoidIn, IdTagMatcher idTagMatcher, EAIData<Integer> avoidDistance, EAIData<Integer> avoidDistanceNear, EAIData<Double> farSpeed, EAIData<Double> nearSpeed) {
		this(entityIn, classToAvoidIn, idTagMatcher, (livingEntity) -> true, avoidDistance, avoidDistanceNear, farSpeed, nearSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> avoidClass, Predicate<LivingEntity> targetPredicate, EAIData<Integer> avoidDistance, EAIData<Integer> avoidDistanceNear, EAIData<Double> farSpeedIn, EAIData<Double> nearSpeedIn, Predicate<LivingEntity> entityPredicate) {
		this(entityIn, avoidClass, null, targetPredicate, avoidDistance, avoidDistanceNear, nearSpeedIn, farSpeedIn, entityPredicate);
	}

	public EAAvoidEntityGoal(PathfinderMob entityIn, Class<T> avoidClass, @Nullable IdTagMatcher idTagMatcher, Predicate<LivingEntity> targetPredicate, EAIData<Integer> avoidDistance, EAIData<Integer> avoidDistanceNear, EAIData<Double> nearSpeedIn, EAIData<Double> farSpeedIn, Predicate<LivingEntity> predicate) {
		this.goalOwner = entityIn;
		this.classToAvoid = avoidClass;
		this.avoidTargetSelector = targetPredicate;
		this.avoidDistance = avoidDistance;
		this.avoidDistanceNear = avoidDistanceNear;
		this.nearSpeed = nearSpeedIn;
		this.farSpeed = farSpeedIn;
		this.predicateOnAvoidEntity = predicate;
		predicate = predicate.and(targetPredicate);
		if (idTagMatcher != null)
			predicate = predicate.and(idTagMatcher::matchesEntity);
		this.builtTargetSelector = TargetingConditions.forCombat().range(this.avoidDistance.get(this.goalOwner)).selector(predicate);
	}

	public boolean canUse() {
		this.avoidTarget = this.goalOwner.level().getNearestEntity(this.classToAvoid, this.builtTargetSelector, this.goalOwner, this.goalOwner.getX(), this.goalOwner.getY(), this.goalOwner.getZ(), this.goalOwner.getBoundingBox().inflate(this.avoidDistance.get(this.goalOwner)));
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
		this.goalOwner.getNavigation().moveTo(this.path, this.farSpeed.get(this.goalOwner));
	}

	public void stop() {
		this.avoidTarget = null;
	}

	public void tick() {
		Integer avoidDistanceNear = this.avoidDistanceNear.get(this.goalOwner);
		if (this.goalOwner.distanceToSqr(this.avoidTarget) < avoidDistanceNear * avoidDistanceNear) {
			this.goalOwner.getNavigation().setSpeedModifier(this.nearSpeed.get(this.goalOwner));
		} else {
			this.goalOwner.getNavigation().setSpeedModifier(this.farSpeed.get(this.goalOwner));
		}

	}

	public void setAttackWhenRunning(boolean attackWhenRunning) {
		if (attackWhenRunning)
			this.setFlags(EnumSet.noneOf(Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.LOOK));
	}
}