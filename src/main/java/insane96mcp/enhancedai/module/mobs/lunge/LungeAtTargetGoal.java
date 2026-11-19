package insane96mcp.enhancedai.module.mobs.lunge;

import insane96mcp.enhancedai.mixin.accessors.MeleeAttackGoalAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public class LungeAtTargetGoal extends Goal {
	/// Distance from the target the mob aims to land at
	private static final double LANDING_DISTANCE = 1.2d;
	/// Roughly the horizontal distance travelled in the air per unit of horizontal speed (air drag is 0.91 per tick, ~9 ticks airborne)
	private static final double AIR_DISTANCE_FACTOR = 6.5d;
	private static final int MAX_AIRBORNE_TICKS = 40;

	protected final Mob mob;
	protected LivingEntity target;
	/// mob.tickCount after which the mob can lunge again. A timestamp is used as canUse is not called every tick
	private int nextLungeTick;
	private int windUpTicksLeft;
	private boolean launched;
	private int airborneTicks;

	public LungeAtTargetGoal(Mob mob) {
		this.mob = mob;
		// Only JUMP so the MeleeAttackGoal (MOVE, LOOK) keeps running while lunging
		this.setFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (this.mob.tickCount < this.nextLungeTick
				|| !this.mob.onGround()
				|| this.mob.isInWater()
				|| this.mob.isPassenger()
				|| this.mob.isVehicle())
			return false;
		LivingEntity target = this.mob.getTarget();
		if (target == null || !target.isAlive() || !this.isInRange(target))
			return false;
		if (Math.abs(target.getY() - this.mob.getY()) > 1.5d
				|| !this.mob.getSensing().hasLineOfSight(target)
				|| this.getRunningMeleeGoal().isEmpty())
			return false;
		this.target = target;
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		if (this.target == null || !this.target.isAlive())
			return false;
		if (!this.launched)
			return !this.mob.isInWater() && this.getRunningMeleeGoal().isPresent();
		// The first ticks after launching the mob might still be on ground
		return this.airborneTicks < 2 || (!this.mob.onGround() && !this.mob.isInWater() && this.airborneTicks < MAX_AIRBORNE_TICKS);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void start() {
		this.launched = false;
		this.airborneTicks = 0;
		this.windUpTicksLeft = LungeAtTarget.WIND_UP_TICKS.getOrDefault(this.mob, LungeAtTarget.windUpTicks);
		if (this.windUpTicksLeft <= 0)
			this.lunge();
		else
			this.standStill();
	}

	@Override
	public void tick() {
		if (this.launched) {
			this.airborneTicks++;
			return;
		}
		this.mob.getLookControl().setLookAt(this.target, 30f, 30f);
		this.standStill();
		this.spawnRunningParticles();
		if (--this.windUpTicksLeft <= 0)
			this.lunge();
	}

	@Override
	public void stop() {
		if (this.launched && this.target != null && this.mob.isWithinMeleeAttackRange(this.target))
			this.getRunningMeleeGoal().ifPresent(meleeAttackGoal -> ((MeleeAttackGoalAccessor) meleeAttackGoal).setTicksUntilNextAttack(0));
		this.nextLungeTick = this.mob.tickCount + LungeAtTarget.COOLDOWN.getOrDefault(this.mob, (int) LungeAtTarget.cooldown.getByDifficulty(this.mob.level()));
		this.windUpTicksLeft = 0;
		this.launched = false;
		this.target = null;
	}

	public boolean isWindingUp() {
		return this.target != null && !this.launched && this.windUpTicksLeft > 0;
	}

	private boolean isInRange(LivingEntity target) {
		double distanceSqr = this.mob.distanceToSqr(target);
		double minDistance = LungeAtTarget.MIN_DISTANCE.getOrDefault(this.mob, LungeAtTarget.minDistance);
		double maxDistance = LungeAtTarget.MAX_DISTANCE.getOrDefault(this.mob, LungeAtTarget.maxDistance);
		return distanceSqr >= minDistance * minDistance && distanceSqr <= maxDistance * maxDistance;
	}

	private void standStill() {
		this.mob.getNavigation().stop();
		this.mob.getMoveControl().setWantedPosition(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 0d);
		Vec3 deltaMovement = this.mob.getDeltaMovement();
		this.mob.setDeltaMovement(0d, deltaMovement.y, 0d);
	}

	private void lunge() {
		this.launched = true;
		Vec3 direction = new Vec3(this.target.getX() - this.mob.getX(), 0d, this.target.getZ() - this.mob.getZ());
		double distance = Math.min(direction.length(), LungeAtTarget.MAX_DISTANCE.getOrDefault(this.mob, LungeAtTarget.maxDistance) + 1d);
		Vec3 horizontal = Vec3.ZERO;
		if (direction.lengthSqr() > 1.0E-7) {
			double strength = Math.max(0d, distance - LANDING_DISTANCE) / AIR_DISTANCE_FACTOR * LungeAtTarget.HORIZONTAL_STRENGTH_MULTIPLIER.getOrDefault(this.mob, LungeAtTarget.horizontalStrengthMultiplier);
			horizontal = direction.normalize().scale(strength);
		}
		this.mob.setDeltaMovement(horizontal.x, LungeAtTarget.VERTICAL_STRENGTH.getOrDefault(this.mob, LungeAtTarget.verticalStrength), horizontal.z);
		this.mob.hasImpulse = true;
		this.mob.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.6f, 0.8f + this.mob.getRandom().nextFloat() * 0.2f);
	}

	/// Server-side version of Entity#spawnSprintParticle
	private void spawnRunningParticles() {
		if (!(this.mob.level() instanceof ServerLevel serverLevel))
			return;
		BlockPos onPos = this.mob.getOnPosLegacy();
		BlockState state = serverLevel.getBlockState(onPos);
		if (state.getRenderShape() == RenderShape.INVISIBLE)
			return;
		double halfWidth = this.mob.getBbWidth() / 2d;
		serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state).setPos(onPos), this.mob.getX(), this.mob.getY() + 0.1d, this.mob.getZ(), 2, halfWidth, 0d, halfWidth, 0d);
	}

	private Optional<MeleeAttackGoal> getRunningMeleeGoal() {
		return this.mob.goalSelector.getAvailableGoals().stream()
				.filter(WrappedGoal::isRunning)
				.map(WrappedGoal::getGoal)
				.filter(goal -> goal instanceof MeleeAttackGoal)
				.map(goal -> (MeleeAttackGoal) goal)
				.findFirst();
	}
}
