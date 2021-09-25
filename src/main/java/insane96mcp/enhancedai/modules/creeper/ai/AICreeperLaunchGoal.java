package insane96mcp.enhancedai.modules.creeper.ai;

import insane96mcp.enhancedai.modules.creeper.utils.CreeperUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;

public class AICreeperLaunchGoal extends Goal {

	protected final CreeperEntity launchingCreeper;
	private LivingEntity creeperAttackTarget;

	private int ticksBeforeLaunching;

	private int cooldown;

	public AICreeperLaunchGoal(CreeperEntity entitycreeperIn) {
		this.launchingCreeper = entitycreeperIn;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		LivingEntity target = this.launchingCreeper.getTarget();
		if (target == null)
			return false;

		if (--cooldown > 0)
			return false;

		if (!this.launchingCreeper.getSensing().canSee(target))
			return false;

		if (this.launchingCreeper.level.getBlockState(this.launchingCreeper.blockPosition().above(3)).getBlock() != Blocks.AIR)
			return false;

		double yDistance = this.launchingCreeper.getY() - target.getY();
		double x = target.getX() - this.launchingCreeper.getX();
		double z = target.getZ() - this.launchingCreeper.getZ();
		double xzDistance = x * x + z * z;

		return xzDistance < activationDistance() && yDistance < CreeperUtils.getExplosionSize(this.launchingCreeper) * 2 && this.launchingCreeper.isOnGround();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
			this.launchingCreeper.getNavigation().stop();
		this.creeperAttackTarget = this.launchingCreeper.getTarget();
		this.launchingCreeper.ignite();
		double distance = this.launchingCreeper.distanceTo(this.creeperAttackTarget);
		this.ticksBeforeLaunching = (int) Math.max((50 - distance) * 0.4d, 1);
	}

	public boolean canContinueToUse() {
		if (this.launchingCreeper.swell == CreeperUtils.getFuse(this.launchingCreeper) - 1 && this.launchingCreeper.distanceToSqr(this.creeperAttackTarget) > (CreeperUtils.getExplosionSizeSq(this.launchingCreeper) * 2d * 2d)) {
			this.cooldown = 120;
			return false;
		}

		return this.creeperAttackTarget != null && this.creeperAttackTarget.isAlive();
	}

	public void tick() {
		this.launchingCreeper.lookAt(this.creeperAttackTarget, 30.0F, 30.0F);
		if (--ticksBeforeLaunching != 0)
			return;

		if (!this.launchingCreeper.level.isClientSide)
			for(ServerPlayerEntity serverplayerentity : ((ServerWorld) this.launchingCreeper.level).players()) {
				((ServerWorld) this.launchingCreeper.level).sendParticles(serverplayerentity, ParticleTypes.CLOUD, true, this.launchingCreeper.getX(), this.launchingCreeper.getY(), this.launchingCreeper.getZ(), 100, 0.5d, 0.5d, 0.5d, 0.2d);
			}

		this.launchingCreeper.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 6.0f, 0.5f);
		double distanceY = this.creeperAttackTarget.getY() - this.launchingCreeper.getY();
		double d0 = this.creeperAttackTarget.getX() - this.launchingCreeper.getX();
		double d2 = this.creeperAttackTarget.getZ() - this.launchingCreeper.getZ();
		double distanceXZ = MathHelper.sqrt(d0 * d0 + d2 * d2);
		Vector3d motion = new Vector3d(d0 * 0.15d, Math.max(distanceY, 6d) / 12d + distanceXZ / 80d, d2 * 0.15d);
		this.launchingCreeper.setDeltaMovement(motion);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.creeperAttackTarget = null;
		this.launchingCreeper.getEntityData().set(CreeperEntity.DATA_IS_IGNITED, false);
	}

	private float activationDistance() {
		float explosionSize = CreeperUtils.getExplosionSize(this.launchingCreeper) * 5;
		return explosionSize * explosionSize;
	}
}