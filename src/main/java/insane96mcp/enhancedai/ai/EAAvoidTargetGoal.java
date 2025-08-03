package insane96mcp.enhancedai.ai;

import insane96mcp.enhancedai.data.EAIData;
import net.minecraft.network.chat.Component;
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
	protected final EAIData<Integer> avoidDistanceFar;
	protected final EAIData<Integer> avoidDistanceNear;
	private final EAIData<Double> farSpeed;
	private final EAIData<Double> nearSpeed;
	protected Path path;
	private final TargetingConditions builtTargetSelector;

	private LivingEntity avoidTarget;

	public EAAvoidTargetGoal(PathfinderMob entityIn, EAIData<Integer> avoidDistanceFar, EAIData<Integer> avoidDistanceNear, EAIData<Double> farSpeed, EAIData<Double> nearSpeed) {
		this(entityIn, avoidDistanceFar, avoidDistanceNear, farSpeed, nearSpeed, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
	}

	public EAAvoidTargetGoal(PathfinderMob entityIn, EAIData<Integer> avoidDistanceFar, EAIData<Integer> avoidDistanceNear, EAIData<Double> farSpeed, EAIData<Double> nearSpeed, Predicate<LivingEntity> entityPredicate) {
		this.goalOwner = entityIn;
		this.avoidDistanceFar = avoidDistanceFar;
		this.avoidDistanceNear = avoidDistanceNear;
		this.nearSpeed = nearSpeed;
		this.farSpeed = farSpeed;
		this.builtTargetSelector = TargetingConditions.forCombat().selector(entityPredicate);
	}

	public boolean canUse() {
		this.goalOwner.setCustomNameVisible(true);
		this.goalOwner.setCustomName(Component.literal(this.goalOwner.tickCount + ""));
		this.avoidTarget = this.goalOwner.getTarget();
		int avoidDistanceFar = this.avoidDistanceFar.get(this.goalOwner);
		if (this.avoidTarget == null
				|| this.goalOwner.distanceToSqr(this.avoidTarget) > avoidDistanceFar * avoidDistanceFar
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
		this.goalOwner.getNavigation().moveTo(this.path, this.farSpeed.get(this.goalOwner));
	}

	public void stop() {
		this.avoidTarget = null;
	}

	public void tick() {
		int nearDistance = this.avoidDistanceNear.get(this.goalOwner);
		if (this.goalOwner.distanceToSqr(this.avoidTarget) < nearDistance * nearDistance) {
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