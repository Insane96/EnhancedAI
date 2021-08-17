package insane96mcp.enhancedai.modules.creeper.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

public class AICreeperLaunchGoal extends Goal {

	protected final CreeperEntity swellingCreeper;
	private LivingEntity creeperAttackTarget;

	private int ticksBeforeLaunching;

	public AICreeperLaunchGoal(CreeperEntity entitycreeperIn) {
		this.swellingCreeper = entitycreeperIn;
		this.setMutexFlags(EnumSet.of(Flag.MOVE));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean shouldExecute() {
		LivingEntity target = this.swellingCreeper.getAttackTarget();
		if (target == null)
			return false;

		double yDistance = this.swellingCreeper.getPosY() - target.getPosY();
		double d0 = target.getPosX() - this.swellingCreeper.getPosX();
		double d2 = target.getPosZ() - this.swellingCreeper.getPosZ();
		double xzDistance = d0 * d0 + d2 * d2;

		return xzDistance < 32 * 32 && yDistance < 4;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.swellingCreeper.getNavigator().clearPath();
		this.creeperAttackTarget = this.swellingCreeper.getAttackTarget();
		this.swellingCreeper.ignite();
		double distance = this.swellingCreeper.getDistance(this.creeperAttackTarget);
		ticksBeforeLaunching = (int) (10 + (32 - distance) / 2);
	}

	public void tick() {
		this.swellingCreeper.getLookController().setLookPosition(this.creeperAttackTarget.getPositionVec());
		if (--ticksBeforeLaunching != 0)
			return;
		double distanceY = this.creeperAttackTarget.getPosY() - this.swellingCreeper.getPosY();
		double d0 = this.creeperAttackTarget.getPosX() - this.swellingCreeper.getPosX();
		double d2 = this.creeperAttackTarget.getPosZ() - this.swellingCreeper.getPosZ();
		double distanceXZ = MathHelper.sqrt(d0 * d0 + d2 * d2);
		double motionX = d0 * 0.16d;
		double motionZ = d2 * 0.16d;
		Vector3d motion = new Vector3d(motionX, distanceY / 12d + distanceXZ / 40d, motionZ);
		this.swellingCreeper.setMotion(motion);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.creeperAttackTarget = null;
	}
}