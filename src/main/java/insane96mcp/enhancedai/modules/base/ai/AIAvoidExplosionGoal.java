package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

public class AIAvoidExplosionGoal extends Goal {
	protected final CreatureEntity entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected Entity avoidTarget;
	protected double explosionRadius;
	protected Path path;
	protected final PathNavigator navigation;

	private boolean run = false;
	private boolean alwaysRun = false;

	public AIAvoidExplosionGoal(CreatureEntity entityIn, double nearSpeedIn, double farSpeedIn) {
		this.entity = entityIn;
		this.farSpeed = farSpeedIn;
		this.nearSpeed = nearSpeedIn;
		this.navigation = entityIn.getNavigator();
		//TODO Check if this causes problems
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean shouldExecute() {
		if (this.run && this.avoidTarget.getDistance(entity) < explosionRadius * 2 && (this.path == null || this.path.isFinished())) {
			Vector3d vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, this.avoidTarget.getPositionVec());
			if (vector3d == null)
				return false;
			this.path = this.navigation.pathfind(vector3d.x, vector3d.y, vector3d.z, 0);
			return this.path != null;
		}
		return false;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return !this.navigation.noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.navigation.setPath(this.path, this.farSpeed);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.avoidTarget = null;
		this.run = false;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.entity.getDistanceSq(this.avoidTarget) < 49.0D) {
			this.entity.getNavigator().setSpeed(this.nearSpeed);
		} else {
			this.entity.getNavigator().setSpeed(this.farSpeed);
		}
	}

	public void run(Entity avoidTarget, double explosionRadius) {
		this.run = true;
		this.avoidTarget = avoidTarget;
		this.explosionRadius = explosionRadius;
	}

	public void setAlwaysRun(boolean alwaysRun) {
		this.alwaysRun = alwaysRun;
	}
}