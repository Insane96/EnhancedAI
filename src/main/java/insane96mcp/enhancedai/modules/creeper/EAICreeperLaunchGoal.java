package insane96mcp.enhancedai.modules.creeper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class EAICreeperLaunchGoal extends Goal {

	protected final net.minecraft.world.entity.monster.Creeper launchingCreeper;
	private LivingEntity creeperAttackTarget;

	private int ticksBeforeLaunching;

	private int cooldown;
	private int fails = 0;
	private boolean hasLaunched = false;

	private int fuse;
	private float explosionSize;
	private float explosionSizeSqr;
	private float activationDistanceSqr;
	private float minActivationDistanceSqr;

	public EAICreeperLaunchGoal(net.minecraft.world.entity.monster.Creeper creeper) {
		this.launchingCreeper = creeper;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	public boolean canUse() {
		// Cache fuse
		if (fuse == 0) {
			fuse = CreeperUtils.getFuse(this.launchingCreeper);
			explosionSize = CreeperUtils.getExplosionSize(this.launchingCreeper);
			explosionSizeSqr = explosionSize * explosionSize;
			activationDistanceSqr = Math.max(144f, explosionSizeSqr * 5f * 5f);
			minActivationDistanceSqr = Math.max(64f, explosionSizeSqr * 3f * 3f);
		}

		LivingEntity target = this.launchingCreeper.getTarget();
		if (target == null)
			return false;

		if (--cooldown > 0)
			return false;

		if (!this.launchingCreeper.getSensing().hasLineOfSight(target) && !Creeper.BREACH.get(this.launchingCreeper))
			return false;

		if (this.launchingCreeper.level().getBlockState(this.launchingCreeper.blockPosition().above(3)).blocksMotion())
			return false;

		//if (this.launchingCreeper.distanceToSqr(target) < 12d * 12d)
			//return false;

		double yDistance = this.launchingCreeper.getY() - target.getY();
		double x = target.getX() - this.launchingCreeper.getX();
		double y = target.getY() - this.launchingCreeper.getY();
		double z = target.getZ() - this.launchingCreeper.getZ();
		double xzDistance = x * x + z * z;
		double distance = x * x + y * y + z * z;

		return xzDistance < activationDistanceSqr && distance > minActivationDistanceSqr && yDistance < explosionSize * 2 && this.launchingCreeper.onGround();
	}

	public void start() {
		this.launchingCreeper.getNavigation().stop();
		this.creeperAttackTarget = this.launchingCreeper.getTarget();
		this.launchingCreeper.ignite();
		double distance = this.launchingCreeper.distanceTo(this.creeperAttackTarget);
		float inaccuracy = 0.1f;
		if (this.launchingCreeper.level().getDifficulty() == Difficulty.EASY)
			inaccuracy = 0.15f;
		if (this.launchingCreeper.level().getDifficulty() == Difficulty.HARD)
			inaccuracy = 0.05f;
		this.ticksBeforeLaunching = (int) Math.max((50 - distance) * Mth.randomBetween(this.launchingCreeper.getRandom(), 0.25f + inaccuracy, 0.25f + inaccuracy), 1);
	}

	public boolean canContinueToUse() {
		if (this.launchingCreeper.swell >= fuse - 2 && this.launchingCreeper.distanceToSqr(this.creeperAttackTarget) > (explosionSizeSqr * 2d * 2d)) {
			this.fails++;
			if (EAICreeperSwellGoal.canCreeperBreach(this.launchingCreeper, this.creeperAttackTarget) || !this.launchingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget))
				this.cooldown = 60 + (this.fails * 60);
			else
				this.cooldown = CreeperUtils.getFuse(this.launchingCreeper);
			return false;
		}
		else if ((this.launchingCreeper.verticalCollision || this.launchingCreeper.horizontalCollision) && this.hasLaunched && EAICreeperSwellGoal.canCreeperBreach(this.launchingCreeper, this.creeperAttackTarget)) {
			this.launchingCreeper.explodeCreeper();
			return false;
		}

		return this.creeperAttackTarget != null && this.creeperAttackTarget.isAlive();
	}

	public void tick() {
		this.launchingCreeper.lookAt(this.creeperAttackTarget, 30.0F, 30.0F);
		if (--ticksBeforeLaunching != 0)
			return;

        ((ServerLevel) this.launchingCreeper.level())
				.players()
				.forEach(player
						-> ((ServerLevel) this.launchingCreeper.level()).sendParticles(player,
						ParticleTypes.FIREWORK,
						true,
						this.launchingCreeper.getX(),
						this.launchingCreeper.getY(),
						this.launchingCreeper.getZ(),
						50,
						0.2d,
						0.2d,
						0.2d,
						0.1d));

		this.launchingCreeper.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 6.0f, 0.5f);
		double distanceY = this.creeperAttackTarget.getY() - this.launchingCreeper.getY();
		double distanceX = this.creeperAttackTarget.getX() - this.launchingCreeper.getX();
		double distanceZ = this.creeperAttackTarget.getZ() - this.launchingCreeper.getZ();
		double distanceXZ = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);

		float inaccuracy = Creeper.LAUNCH_INACCURACY.get(this.launchingCreeper).floatValue();
		distanceX += Mth.randomBetween(this.launchingCreeper.getRandom(), -inaccuracy, inaccuracy);
		distanceZ += Mth.randomBetween(this.launchingCreeper.getRandom(), -inaccuracy, inaccuracy);
		//TODO better Y speed, right now when creeper Y distance is below 7 you always get 7 which isn't good when the creeper's Y distance is 0, and when the Y Distance is higher than about 25 the creeper will go to space
		Vec3 motion = new Vec3(distanceX * 0.2d, Mth.clamp(distanceY, 6d, 40d) / 10d + distanceXZ / 72d, distanceZ * 0.2d);
		this.launchingCreeper.setDeltaMovement(motion);
		this.hasLaunched = true;
	}

	public void stop() {
		this.creeperAttackTarget = null;
		this.hasLaunched = false;
		this.launchingCreeper.getEntityData().set(net.minecraft.world.entity.monster.Creeper.DATA_IS_IGNITED, false);
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}