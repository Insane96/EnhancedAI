package insane96mcp.enhancedai.modules.mobs.avoidexplosion;

import insane96mcp.enhancedai.data.EAIData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AvoidExplosionGoal extends Goal {
	protected final PathfinderMob entity;
	private final EAIData<Double> farSpeed;
	private final EAIData<Double> nearSpeed;
	protected Entity avoidTarget;
	protected double explosionRadius;
	protected Path path;

	private boolean run = false;
	//private boolean alwaysRun = false;

	public AvoidExplosionGoal(PathfinderMob entityIn, EAIData<Double> farSpeed, EAIData<Double> nearSpeed) {
		this.entity = entityIn;
		this.farSpeed = farSpeed;
		this.nearSpeed = nearSpeed;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	public boolean canUse() {
		if (this.run && this.avoidTarget.distanceToSqr(entity) < getFarDistanceSqr() && (this.path == null || this.path.isDone())) {
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
				&& !this.avoidTarget.isRemoved()
				&& this.entity.distanceToSqr(this.avoidTarget) < this.getFarDistanceSqr()
				&& this.entity.getNavigation().isInProgress();
	}

	public void start() {
		this.entity.getNavigation().moveTo(this.path, this.farSpeed.get(this.entity));
		this.entity.stopRiding();
	}

	public void stop() {
		this.avoidTarget = null;
		this.path = null;
		this.run = false;
	}

	public void tick() {
		if (this.entity.distanceToSqr(this.avoidTarget) < this.getNearDistanceSqr())
			this.entity.getNavigation().setSpeedModifier(this.nearSpeed.get(this.entity));
		else
			this.entity.getNavigation().setSpeedModifier(this.farSpeed.get(this.entity));
	}

	public void runFrom(Entity avoidTarget, double explosionRadius) {
		this.run = true;
		this.avoidTarget = avoidTarget;
		this.explosionRadius = explosionRadius;
	}

	private double getFarDistance() {
		return this.explosionRadius * 2f;
	}

	private double getFarDistanceSqr() {
		return this.getFarDistance() * this.getFarDistance();
	}

	private double getNearDistance() {
		return this.explosionRadius;
	}

	private double getNearDistanceSqr() {
		return this.getNearDistance() * this.getNearDistance();
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}