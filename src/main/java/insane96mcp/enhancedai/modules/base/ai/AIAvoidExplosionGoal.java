package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

public class AIAvoidExplosionGoal extends Goal {
	protected final CreatureEntity entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected Entity avoidTarget;
	protected double explosionRadius;
	protected Path path;

	private boolean run = false;
	//private boolean alwaysRun = false;

	public AIAvoidExplosionGoal(CreatureEntity entityIn, double nearSpeedIn, double farSpeedIn) {
		this.entity = entityIn;
		this.farSpeed = farSpeedIn;
		this.nearSpeed = nearSpeedIn;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean shouldExecute() {
		if (this.run && this.avoidTarget.getDistanceSq(entity) < (explosionRadius * 2 * explosionRadius * 2) && (this.path == null || this.path.isFinished())) {
			Vector3d vector3d;
			int t = 0;
			do {
				vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, this.avoidTarget.getPositionVec());
				t++;
			} while (vector3d == null && t < 5);
			if (vector3d == null)
				return false;
			this.path = this.entity.getNavigator().pathfind(vector3d.x, vector3d.y, vector3d.z, 0);
			return this.path != null;
		}
		return false;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return this.avoidTarget != null && !this.avoidTarget.isAlive();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.entity.getNavigator().setPath(this.path, this.farSpeed);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.avoidTarget = null;
		this.path = null;
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

		//TODO Make them run again if too near the explosion
		/*if (this.entity.getNavigator().noPath()) {
			Vector3d vector3d;
			int t = 0;
			do {
				vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.entity, 16, 7, this.avoidTarget.getPositionVec());
				t++;
			} while (vector3d == null && t < 5);
			if (vector3d != null) {
				this.path = this.entity.getNavigator().pathfind(vector3d.x, vector3d.y, vector3d.z, 0);
				this.entity.getNavigator().setPath(this.path, this.farSpeed);
			}
		}*/
	}

	public void run(Entity avoidTarget, double explosionRadius) {
		this.run = true;
		this.avoidTarget = avoidTarget;
		this.explosionRadius = explosionRadius;
	}

	/*public void setAlwaysRun(boolean alwaysRun) {
		this.alwaysRun = alwaysRun;
	}*/
}