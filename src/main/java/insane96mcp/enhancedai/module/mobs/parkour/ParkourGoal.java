package insane96mcp.enhancedai.module.mobs.parkour;

import insane96mcp.enhancedai.ai.EAIRangedAttackGoal;
import insane96mcp.enhancedai.module.illager.shoot.EAIPillagerAttackGoal;
import insane96mcp.enhancedai.utils.GoalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
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

    private static final int MAX_FORWARD_BLOCK_CHECK = 3;
    private static final double FORWARD_STEP_EPSILON = 0.1d;
    private static final double SLIGHT_DOWNWARD_NUDGE = -0.01d;

    @Override
    public boolean canUse() {
        if (!this.goalOwner.onGround()
                || this.goalOwner.isPassenger()
                || hasConflictingRunningGoals()) {
            this.resetLastPosition();
            return false;
        }

        this.target = this.goalOwner.getTarget();
        if (this.target == null || !this.goalOwner.hasLineOfSight(this.target)) {
            this.resetLastPosition();
            return false;
        }

        if (!this.isStuck()) {
            return false;
        }

        Vec3 toTargetDir = new Vec3(
                this.target.getX() - this.goalOwner.getX(),
                this.target.getY() - this.goalOwner.getY(),
                this.target.getZ() - this.goalOwner.getZ()
        ).normalize();

        // TODO Check 1 block above and below
        this.jumpBlocks = findJumpBlocksTowardsTarget(toTargetDir);

        return this.jumpBlocks > 0;
    }

    private boolean hasConflictingRunningGoals() {
        return GoalHelper.isRunning(this.goalOwner.goalSelector, this::isConflictingGoal);
    }

    private boolean isConflictingGoal(Goal goal) {
        return goal instanceof RangedAttackGoal
                || goal instanceof EAIRangedAttackGoal
                || goal instanceof EAIPillagerAttackGoal;
    }

    private int findJumpBlocksTowardsTarget(Vec3 toTargetDir) {
        for (int offsetBlocks = 1; offsetBlocks <= MAX_FORWARD_BLOCK_CHECK; offsetBlocks++) {
            Vec3 probe = this.goalOwner.position()
                    .add(toTargetDir.scale(offsetBlocks + FORWARD_STEP_EPSILON))
                    .add(0.0, SLIGHT_DOWNWARD_NUDGE, 0.0);
            BlockPos candidatePos = BlockPos.containing(probe);
            if (this.goalOwner.level().getBlockState(candidatePos).isSolid()) {
                return offsetBlocks;
            }
        }
        return 0;
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
