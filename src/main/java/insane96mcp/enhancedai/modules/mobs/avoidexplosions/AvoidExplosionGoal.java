package insane96mcp.enhancedai.modules.mobs.avoidexplosions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AvoidExplosionGoal extends Goal {
	protected final PathfinderMob entity;
	private final double farSpeed;
	private final double nearSpeed;
	protected Entity avoidTarget;
	protected double explosionRadius;
	protected Path path;

	private boolean run = false;
	//private boolean alwaysRun = false;

	public AvoidExplosionGoal(PathfinderMob entityIn, double nearSpeedIn, double farSpeedIn) {
		this.entity = entityIn;
		this.farSpeed = farSpeedIn;
		this.nearSpeed = nearSpeedIn;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	public boolean canUse() {
		if (this.run && this.avoidTarget.distanceToSqr(entity) < (explosionRadius * explosionRadius * 2d * 2d) && (this.path == null || this.path.isDone())) {
			Vec3 vector3d;
			int t = 0;
			do {
				vector3d = DefaultRandomPos.getPosAway(this.entity, 16, 7, this.avoidTarget.position());
				t++;
			} while (vector3d == null && t < 5);
			if (vector3d == null)
				return false;
			this.path = this.entity.getNavigation().createPath(vector3d.x, vector3d.y, vector3d.z, 0);
			return this.path != null;
		}
		return false;
	}

	public boolean canContinueToUse() {
		return this.avoidTarget != null
				&& this.avoidTarget.isAlive()
				&& this.entity.distanceToSqr(this.avoidTarget) < this.explosionRadius * this.explosionRadius
				&& this.entity.getNavigation().isInProgress();
	}

	public void start() {
		this.entity.getNavigation().moveTo(this.path, this.farSpeed);
	}

	public void stop() {
		this.avoidTarget = null;
		this.path = null;
		this.run = false;
	}

	public void tick() {
		if (this.entity.distanceToSqr(this.avoidTarget) < 49.0D) {
			this.entity.getNavigation().setSpeedModifier(this.nearSpeed);
		} else {
			this.entity.getNavigation().setSpeedModifier(this.farSpeed);
		}
	}

	public void run(Entity avoidTarget, double explosionRadius) {
		this.run = true;
		this.avoidTarget = avoidTarget;
		this.explosionRadius = explosionRadius;
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}