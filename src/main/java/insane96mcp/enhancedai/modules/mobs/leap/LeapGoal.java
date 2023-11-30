package insane96mcp.enhancedai.modules.mobs.leap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class LeapGoal extends Goal {
	protected LivingEntity target;
	protected Mob goalOwner;
	protected int ticksWithoutPath;


	public LeapGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (!this.goalOwner.onGround())
			return false;
		this.target = this.goalOwner.getTarget();
		if (this.target == null)
			return false;
		if (this.goalOwner.getNavigation().isDone() || this.goalOwner.getNavigation().isStuck())
			ticksWithoutPath++;
		else {
			ticksWithoutPath = 0;
			return false;
		}
		return this.goalOwner.distanceToSqr(this.target) < 100 && ticksWithoutPath > adjustedTickDelay(15);
	}

	@Override
	public void stop() {
		this.ticksWithoutPath = 0;
	}

	@Override
	public void start() {
		this.goalOwner.setJumping(true);
		double distanceY = this.target.getY() - this.goalOwner.getY();
		double distanceX = this.target.getX() - this.goalOwner.getX();
		double distanceZ = this.target.getZ() - this.goalOwner.getZ();

		this.goalOwner.setDeltaMovement(new Vec3(distanceX, distanceY, distanceZ).normalize().add(0, 0.2d, 0));
		this.stop();
	}
}
