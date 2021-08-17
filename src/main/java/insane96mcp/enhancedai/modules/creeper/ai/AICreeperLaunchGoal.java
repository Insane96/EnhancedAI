package insane96mcp.enhancedai.modules.creeper.ai;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

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

		if (!this.swellingCreeper.getEntitySenses().canSee(target))
			return false;

		if (this.swellingCreeper.world.getBlockState(this.swellingCreeper.getPosition().up(3)).getBlock() != Blocks.AIR)
			return false;

		double yDistance = this.swellingCreeper.getPosY() - target.getPosY();
		double d0 = target.getPosX() - this.swellingCreeper.getPosX();
		double d2 = target.getPosZ() - this.swellingCreeper.getPosZ();
		double xzDistance = d0 * d0 + d2 * d2;

		return xzDistance < 32 * 32 && yDistance < 4 && this.swellingCreeper.isOnGround();
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

	public boolean shouldContinueExecuting() {
		return this.creeperAttackTarget != null && !this.creeperAttackTarget.dead;
	}

	public void tick() {
		this.swellingCreeper.faceEntity(this.creeperAttackTarget, 30.0F, 30.0F);
		if (--ticksBeforeLaunching != 0)
			return;

		if (!this.swellingCreeper.world.isRemote)
			for(ServerPlayerEntity serverplayerentity : ((ServerWorld) this.swellingCreeper.world).getPlayers())
				((ServerWorld) this.swellingCreeper.world).spawnParticle(serverplayerentity, ParticleTypes.CLOUD, true, this.swellingCreeper.getPosX(), this.swellingCreeper.getPosY(), this.swellingCreeper.getPosZ(), 100, 0.5d, 0.5d, 0.5d, 0.2d);
		double distanceY = this.creeperAttackTarget.getPosY() - this.swellingCreeper.getPosY();
		double d0 = this.creeperAttackTarget.getPosX() - this.swellingCreeper.getPosX();
		double d2 = this.creeperAttackTarget.getPosZ() - this.swellingCreeper.getPosZ();
		double distanceXZ = MathHelper.sqrt(d0 * d0 + d2 * d2);
		Vector3d motion = new Vector3d(d0 * 0.16d, distanceY / 12d + distanceXZ / 40d, d2 * 0.16d);
		this.swellingCreeper.setMotion(motion);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.creeperAttackTarget = null;
	}
}