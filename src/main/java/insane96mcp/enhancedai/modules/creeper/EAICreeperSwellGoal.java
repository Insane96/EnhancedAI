package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.ai.BetaStrafe;
import insane96mcp.enhancedai.modules.mobs.avoidexplosion.AvoidExplosionGoal;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EAICreeperSwellGoal extends Goal {

	private static final UUID WALKING_FUSE_SPEED_MODIFIER_UUID = UUID.fromString("ab376fec-5a15-4d3e-8fa2-0be4b6bc1849");

	protected final Creeper swellingCreeper;
	private LivingEntity creeperAttackTarget;

	private boolean walkingFuse = false;

	private boolean isBreaching = false;

	private float explosionSize;
	private float explosionSizeSqr;

	@SuppressWarnings("FieldCanBeLocal")
	private final double IGNITE_DISTANCE_MULTIPLIER_SQR = 1.35d * 1.35d;

    @Nullable
	BetaStrafe betaStrafe;

	private Vec3 lastPosition = null;
	private int lastPositionTickstamp = 0;

	public EAICreeperSwellGoal(Creeper creeper) {
		this.swellingCreeper = creeper;
	}

	public boolean canUse() {
		if (explosionSize == 0f) {
			//Cache the explosion size
			explosionSize = CreeperUtils.getExplosionSize(this.swellingCreeper);
			explosionSizeSqr = explosionSize * explosionSize;
		}

		this.creeperAttackTarget = this.swellingCreeper.getTarget();
		if (creeperAttackTarget == null)
			return false;

		this.isBreaching = CreeperSwell.BREACH.get(this.swellingCreeper) && canBreach(this.creeperAttackTarget);

		return (this.swellingCreeper.getSwellDir() > 0) ||
				this.isBreaching ||
				((this.swellingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget) || CreeperSwell.IGNORE_WALLS.get(this.swellingCreeper))
						&& this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) < explosionSizeSqr * IGNITE_DISTANCE_MULTIPLIER_SQR);
	}

	public void start() {
        if (walkingFuse && this.betaStrafe == null)
            MCUtils.applyModifier(this.swellingCreeper, Attributes.MOVEMENT_SPEED, WALKING_FUSE_SPEED_MODIFIER_UUID, "Walking fuse speed modifier", CreeperSwell.WALKING_FUSE_SPEED_MODIFIER.get(this.swellingCreeper), AttributeModifier.Operation.MULTIPLY_BASE, false);
        else
            this.swellingCreeper.getNavigation().stop();
        this.swellingCreeper.setSwellDir(1);
		this.swellingCreeper.lookAt(this.creeperAttackTarget, 30f, 30f);
        if (this.betaStrafe != null)
            this.betaStrafe.start();
		//Update the explosion size in case the creeper becomes charged
		explosionSize = CreeperUtils.getExplosionSize(this.swellingCreeper);
		explosionSizeSqr = explosionSize * explosionSize;
		if (CreeperSwell.insaneSurvivalOverhaulIntegration)
			this.swellingCreeper.getPersistentData().putFloat("iguanatweaksreborn:explosion_ray_strength_multiplier", this.isBreaching ? 0.01f : 0.3f);
	}

	public void stop() {
		this.creeperAttackTarget = null;
		this.isBreaching = false;
		this.swellingCreeper.setSwellDir(-1);
		AttributeInstance movementSpeed = this.swellingCreeper.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movementSpeed != null)
			movementSpeed.removeModifier(WALKING_FUSE_SPEED_MODIFIER_UUID);
	}

	public void tick() {
		if (this.creeperAttackTarget == null || !this.creeperAttackTarget.isAlive())
			this.tryCancelSwell();
		/*if (this.isBreaching && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) >= CreeperSwell.breachHorizontalRange * CreeperSwell.breachHorizontalRange)
			this.tryCancelSwell();*/
		else if (this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) > (explosionSizeSqr * 2d * 2d) && !isBreaching)
			this.tryCancelSwell();
		else if (!this.swellingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget) && !CreeperSwell.IGNORE_WALLS.get(this.swellingCreeper) && !isBreaching)
			this.tryCancelSwell();
		else {
			if (this.swellingCreeper.tickCount % 5 == 0) {
				this.swellingCreeper.setSwellDir(1);
				alertNearby();
			}
			this.swellingCreeper.lookAt(this.creeperAttackTarget, 30f, 30f);
            if (this.betaStrafe != null)
                this.betaStrafe.tick();
		}
	}

	private void alertNearby() {
		List<PathfinderMob> creaturesNearby = this.swellingCreeper.level().getEntitiesOfClass(PathfinderMob.class, this.swellingCreeper.getBoundingBox().inflate(explosionSize * 2));
		for (PathfinderMob creatureEntity : creaturesNearby) {
			if (creatureEntity == this.swellingCreeper
					|| creatureEntity == this.swellingCreeper.getVehicle())
				continue;
			creatureEntity.goalSelector.availableGoals.forEach(prioritizedGoal -> {
				if (prioritizedGoal.getGoal() instanceof AvoidExplosionGoal avoidExplosionGoal) {
					avoidExplosionGoal.runFrom(this.swellingCreeper, explosionSize);
				}
			});
		}
	}

	private void tryCancelSwell() {
		if (!CreeperSwell.FORCE_EXPLODE.get(this.swellingCreeper))
			this.swellingCreeper.setSwellDir(-1);
	}

	public void setWalkingFuse(boolean walkingFuse) {
		if (walkingFuse)
			this.setFlags(EnumSet.noneOf(Goal.Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.MOVE));

		this.walkingFuse = walkingFuse;
	}

	public void setBetaStrafe(boolean beta) {
        if (beta)
            this.betaStrafe = new BetaStrafe(this.swellingCreeper, CreeperSwell.BETA_LEFT_STRAFE.get(this.swellingCreeper), 0.5d);
        else
            this.betaStrafe = null;
	}

	public boolean canBreach(LivingEntity target) {
		if (!CreeperSwell.BREACH.get(this.swellingCreeper))
			return false;
		double yDistance = this.swellingCreeper.getY() - target.getY();
		double x = target.getX() - this.swellingCreeper.getX();
		double z = target.getZ() - this.swellingCreeper.getZ();
		double xzDistance = x * x + z * z;
		double horizontalRange = CreeperSwell.BREACH_HORIZONTAL_RANGE.get(this.swellingCreeper);
		return this.isStuck()
				&& !this.swellingCreeper.getSensing().hasLineOfSight(target)
				&& !this.swellingCreeper.isInWater()
				&& xzDistance < horizontalRange * horizontalRange
				&& yDistance > -CreeperUtils.getExplosionSize(this.swellingCreeper) - 2;
	}

	public static boolean canCreeperBreach(Creeper creeper, LivingEntity target) {
		Set<WrappedGoal> availableGoals = creeper.goalSelector.getAvailableGoals();

		return availableGoals.stream()
				.filter(wrappedGoal -> wrappedGoal.getGoal() instanceof EAICreeperSwellGoal)
				.anyMatch(eaCreeperSwellGoal -> ((EAICreeperSwellGoal) eaCreeperSwellGoal.getGoal()).canBreach(target));
	}

	/**
	 * Returns true if the creeper has been stuck in the same spot (radius 1.5 blocks) for more than 3 seconds
	 */
	public boolean isStuck() {
		if (this.swellingCreeper.getTarget() == null)
			return false;

		if (this.lastPosition == null || this.swellingCreeper.distanceToSqr(this.lastPosition) > 1d) {
			this.lastPosition = this.swellingCreeper.position();
			this.lastPositionTickstamp = this.swellingCreeper.tickCount;
		}
		return this.swellingCreeper.getNavigation().isDone() || this.swellingCreeper.tickCount - this.lastPositionTickstamp >= 30;
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}