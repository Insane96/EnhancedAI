package insane96mcp.enhancedai.ai;

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

public class EAAvoidTargetGoal extends Goal {
	protected final PathfinderMob goalOwner;
	private final double farSpeed;
	private final double nearSpeed;
	protected final float avoidDistance;
	protected final float avoidDistanceNear;
	protected Path path;
	private final TargetingConditions builtTargetSelector;

	private LivingEntity avoidTarget;

	public EAAvoidTargetGoal(PathfinderMob entityIn, float avoidDistance, float avoidDistanceNear, double nearSpeed, double farSpeed) {
		this(entityIn, (livingEntity) -> true, avoidDistance, avoidDistanceNear, nearSpeed, farSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public EAAvoidTargetGoal(PathfinderMob entityIn, Predicate<LivingEntity> targetPredicate, float avoidDistance, float avoidDistanceNear, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> p_i48859_9_) {
		this.goalOwner = entityIn;
		this.avoidDistance = avoidDistance * avoidDistance;
		this.avoidDistanceNear = avoidDistanceNear * avoidDistanceNear;
		this.nearSpeed = nearSpeedIn;
		this.farSpeed = farSpeedIn;
		this.builtTargetSelector = TargetingConditions.forCombat().range(avoidDistance).selector(p_i48859_9_.and(targetPredicate));
	}

	public boolean canUse() {
		this.avoidTarget = this.goalOwner.getTarget();
        if (this.avoidTarget == null
				|| this.goalOwner.distanceToSqr(this.avoidTarget) > this.avoidDistance
				|| !this.builtTargetSelector.test(this.goalOwner, this.avoidTarget))
            return false;

        Vec3 posAway = DefaultRandomPos.getPosAway(this.goalOwner, 16, 7, this.avoidTarget.position());
        if (posAway == null
				|| this.avoidTarget.distanceToSqr(posAway) < this.avoidTarget.distanceToSqr(this.goalOwner))
            return false;

        this.path = this.goalOwner.getNavigation().createPath(posAway.x, posAway.y, posAway.z, 0);
		return this.path != null;
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
		if (this.goalOwner.distanceToSqr(this.avoidTarget) < this.avoidDistanceNear) {
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