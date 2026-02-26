package insane96mcp.enhancedai.module.spider;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.phys.Vec3;

public class DestuckWallGoal extends Goal {

	public Spider spider;

	private Vec3 lastPosition = null;
	private int lastPositionTickstamp = Integer.MAX_VALUE;

	private int preventClimbTick;

	public DestuckWallGoal(Spider spider) {
		this.spider = spider;
	}

	@Override
	public boolean canUse() {
		return isStuck() && this.spider.isClimbing();
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	@Override
	public void start() {
		if (this.spider.getTarget() != null) {
			Vec3 direction = this.spider.getTarget().position().subtract(this.spider.position()).normalize();
			boolean canSeeTarget = this.spider.getSensing().hasLineOfSight(this.spider.getTarget());
			if (!canSeeTarget)
				direction = new Vec3(this.spider.level().getRandom().nextGaussian(), 0, this.spider.level().getRandom().nextGaussian());
			this.spider.setDeltaMovement(this.spider.getDeltaMovement().add(direction.scale(0.75d)));
		}
	}

	@Override
	public void stop() {
		this.resetLastPosition();
	}

	/**
	 * Returns true if the mob has been stuck in the same spot (radius 0.6 blocks) for more than 3 seconds
	 */
	public boolean isStuck() {
		if (this.lastPosition == null || this.spider.distanceToSqr(this.lastPosition) > 0.36d) {
			this.lastPosition = this.spider.position();
			this.lastPositionTickstamp = this.spider.tickCount;
		}
		return /*this.goalOwner.getNavigation().isDone() ||*/ this.spider.tickCount - this.lastPositionTickstamp >= reducedTickDelay(40);
	}

	public void resetLastPosition() {
		this.lastPosition = null;
		this.lastPositionTickstamp = Integer.MAX_VALUE;
	}
}
