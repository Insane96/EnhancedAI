package insane96mcp.enhancedai.modules.creeper.ai;

import insane96mcp.enhancedai.modules.creeper.utils.CreeperUtils;
import insane96mcp.enhancedai.setup.EAStrings;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AICreeperLaunchGoal extends Goal {

	protected final Creeper launchingCreeper;
	private LivingEntity creeperAttackTarget;

	private int ticksBeforeLaunching;

	private int cooldown;
	private int fails = 0;
	private boolean hasLaunched = false;

	private int fuse;
	private float explosionSize;
	private float explosionSizeSqr;
	private float activationDistanceSqr;

	public AICreeperLaunchGoal(Creeper creeper) {
		this.launchingCreeper = creeper;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	public boolean canUse() {
		if (fuse == 0) {
			fuse = CreeperUtils.getFuse(this.launchingCreeper);
			explosionSize = CreeperUtils.getExplosionSize(this.launchingCreeper);
			explosionSizeSqr = explosionSize * explosionSize;
			activationDistanceSqr = explosionSizeSqr * 5f * 5f;
		}

		LivingEntity target = this.launchingCreeper.getTarget();
		if (target == null)
			return false;

		if (--cooldown > 0)
			return false;

		if (!this.launchingCreeper.getSensing().hasLineOfSight(target) && !this.launchingCreeper.getPersistentData().contains(EAStrings.Tags.Creeper.BREACH))
			return false;

		if (this.launchingCreeper.level.getBlockState(this.launchingCreeper.blockPosition().above(3)).getMaterial().blocksMotion())
			return false;

		if (this.launchingCreeper.distanceToSqr(target) < 12d * 12d)
			return false;

		double yDistance = this.launchingCreeper.getY() - target.getY();
		double x = target.getX() - this.launchingCreeper.getX();
		double z = target.getZ() - this.launchingCreeper.getZ();
		double xzDistance = x * x + z * z;

		return xzDistance < activationDistanceSqr && yDistance < explosionSize * 2 && this.launchingCreeper.isOnGround();
	}

	public void start() {
		this.launchingCreeper.getNavigation().stop();
		this.creeperAttackTarget = this.launchingCreeper.getTarget();
		this.launchingCreeper.ignite();
		double distance = this.launchingCreeper.distanceTo(this.creeperAttackTarget);
		this.ticksBeforeLaunching = (int) Math.max((50 - distance) * 0.33d, 1);
	}

	public boolean canContinueToUse() {
		if (this.launchingCreeper.swell >= fuse - 2 && this.launchingCreeper.distanceToSqr(this.creeperAttackTarget) > (explosionSizeSqr * 2d * 2d)) {
			this.fails++;
			this.cooldown = 60 + (this.fails * 60);
			return false;
		}
		else if ((this.launchingCreeper.verticalCollision || this.launchingCreeper.horizontalCollision) && this.hasLaunched && AICreeperSwellGoal.canBreach(this.launchingCreeper, this.creeperAttackTarget)) {
			this.launchingCreeper.explodeCreeper();
			return false;
		}

		return this.creeperAttackTarget != null && this.creeperAttackTarget.isAlive();
	}

	public void tick() {
		this.launchingCreeper.lookAt(this.creeperAttackTarget, 30.0F, 30.0F);
		if (--ticksBeforeLaunching != 0)
			return;

		if (!this.launchingCreeper.level.isClientSide) {
			for (ServerPlayer player : ((ServerLevel) this.launchingCreeper.level).players()) {
				((ServerLevel) this.launchingCreeper.level).sendParticles(player, ParticleTypes.CLOUD, true, this.launchingCreeper.getX(), this.launchingCreeper.getY(), this.launchingCreeper.getZ(), 100, 0.5d, 0.5d, 0.5d, 0.2d);
			}
		}

		this.launchingCreeper.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 6.0f, 0.5f);
		double distanceY = this.creeperAttackTarget.getY() - this.launchingCreeper.getY();
		double d0 = this.creeperAttackTarget.getX() - this.launchingCreeper.getX();
		double d2 = this.creeperAttackTarget.getZ() - this.launchingCreeper.getZ();
		double distanceXZ = Math.sqrt(d0 * d0 + d2 * d2);

		//TODO better Y speed, right now when creeper Y distance is below 7 you always get 7 which isn't good when the creeper's Y distance is 0, and when the Y Distance is higher than about 25 the creeper will go to space
		Vec3 motion = new Vec3(d0 * 0.15d, Mth.clamp(distanceY, 7d, 40d) / 10d + distanceXZ / 72d, d2 * 0.15d);
		this.launchingCreeper.setDeltaMovement(motion);
		this.hasLaunched = true;
	}

	public void stop() {
		this.creeperAttackTarget = null;
		this.hasLaunched = false;
		this.launchingCreeper.getEntityData().set(Creeper.DATA_IS_IGNITED, false);
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}