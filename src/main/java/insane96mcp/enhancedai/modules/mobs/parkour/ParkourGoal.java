package insane96mcp.enhancedai.modules.mobs.parkour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ParkourGoal extends Goal {
	protected LivingEntity target;
	protected Mob goalOwner;

	private int jumpBlocks = 0;
	private Vec3 lastPosition = null;
	private int lastPositionTickstamp = Integer.MAX_VALUE;

	private boolean waitForLanding = false;

	public ParkourGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
		this.setFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (!this.goalOwner.onGround()
				|| this.goalOwner.isPassenger()) {
			this.resetLastPosition();
			return false;
		}
		this.target = this.goalOwner.getTarget();
		if (this.target == null
				|| !this.goalOwner.hasLineOfSight(this.target)) {
			this.resetLastPosition();
			return false;
		}
		if (!this.isStuck())
			return false;
		Vec3 direction = new Vec3(this.target.getX() - this.goalOwner.getX(), this.target.getY() - this.goalOwner.getY(), this.target.getZ() - this.goalOwner.getZ()).normalize();

		//TODO Check 1 block above and below
		for (int i = 1; i <= 3; i++) {
			BlockPos pos = BlockPos.containing(this.goalOwner.position().add(direction.scale(i + 0.1d)).add(0.0, -0.01d, 0.0));
			if (this.goalOwner.level().getBlockState(pos).isSolid()) {
				jumpBlocks = i;
				break;
			}
		}
		return jumpBlocks > 0;
	}

	@Override
	public boolean canContinueToUse() {
		return waitForLanding && !this.goalOwner.onGround();
	}

	@Override
	public void stop() {
		this.resetLastPosition();
		this.jumpBlocks = 0;
		this.waitForLanding = false;
		this.goalOwner.getNavigation().stop();
		if (this.target != null && !this.target.isDeadOrDying() && !this.target.isRemoved())
			this.goalOwner.getNavigation().moveTo(this.target, 1f);
	}

	@Override
	public void start() {
		this.goalOwner.getJumpControl().jump();
		double distanceY = this.target.getY() - this.goalOwner.getY();
		double distanceX = this.target.getX() - this.goalOwner.getX();
		double distanceZ = this.target.getZ() - this.goalOwner.getZ();

		double factor = 0.65d - ((3 - jumpBlocks) * 0.2d);
		this.goalOwner.setDeltaMovement(this.goalOwner.getDeltaMovement().add(new Vec3(distanceX, distanceY, distanceZ).normalize()).multiply(factor, 1, factor));
		this.waitForLanding = true;
	}

	/**
	 * Returns true if the mob has been stuck in the same spot (radius 0.6 blocks) for more than 3 seconds
	 */
	public boolean isStuck() {
		if (this.goalOwner.getTarget() == null
				|| this.goalOwner.distanceToSqr(this.goalOwner.getTarget()) < 1) {
			this.resetLastPosition();
			return false;
		}

		if (this.lastPosition == null || this.goalOwner.distanceToSqr(this.lastPosition) > 0.36d) {
			this.lastPosition = this.goalOwner.position();
			this.lastPositionTickstamp = this.goalOwner.tickCount;
		}
		return /*this.goalOwner.getNavigation().isDone() ||*/ this.goalOwner.tickCount - this.lastPositionTickstamp >= this.adjustedTickDelay(40);
	}

	public void resetLastPosition() {
		this.lastPosition = null;
		this.lastPositionTickstamp = Integer.MAX_VALUE;
	}
}
