package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.modules.mobs.avoidexplosion.AvoidExplosionGoal;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class EACreeperSwellGoal extends Goal {

	private static final UUID WALKING_FUSE_SPEED_MODIFIER_UUID = UUID.fromString("ab376fec-5a15-4d3e-8fa2-0be4b6bc1849");

	protected final Creeper swellingCreeper;
	private LivingEntity creeperAttackTarget;

	private boolean walkingFuse = false;
	private boolean ignoreWalls = false;
	private boolean breaching = false;

	private boolean isBreaching = false;
	private boolean forceExplode = false;

	private float explosionSize;
	private float explosionSizeSqr;

	@SuppressWarnings("FieldCanBeLocal")
	private final double IGNITE_DISTANCE_MULTIPLIER_SQR = 1.35d * 1.35d;

	boolean beta = false;
	float angle = 0;
	boolean betaStrafeLeft;

	public EACreeperSwellGoal(Creeper creeper) {
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

		boolean canBreach = breaching && canBreach(this.swellingCreeper, this.creeperAttackTarget);
		boolean ignoresWalls = ignoreWalls && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) < explosionSizeSqr;

		if (canBreach)
			isBreaching = true;

		return (this.swellingCreeper.getSwellDir() > 0) ||
				ignoresWalls ||
				canBreach ||
				(this.swellingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget) && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) < explosionSizeSqr * IGNITE_DISTANCE_MULTIPLIER_SQR);
	}

	public void start() {
		if (!walkingFuse)
			this.swellingCreeper.getNavigation().stop();
		else
			MCUtils.applyModifier(this.swellingCreeper, Attributes.MOVEMENT_SPEED, WALKING_FUSE_SPEED_MODIFIER_UUID, "Walking fuse speed modifier", CreeperSwell.walkingFuseSpeedModifier, AttributeModifier.Operation.MULTIPLY_BASE, false);
		this.swellingCreeper.setSwellDir(1);
		this.swellingCreeper.lookAt(this.creeperAttackTarget, 30f, 30f);
		this.angle = (float) Math.toDegrees(Math.atan2(this.swellingCreeper.getZ() - this.creeperAttackTarget.getZ(), this.swellingCreeper.getX() - this.creeperAttackTarget.getX())) - 90;
		if (this.betaStrafeLeft)
			this.angle += 180;
		//Update the explosion size in case the creeper becomes charged
		explosionSize = CreeperUtils.getExplosionSize(this.swellingCreeper);
		explosionSizeSqr = explosionSize * explosionSize;
	}

	public void stop() {
		this.creeperAttackTarget = null;
		this.isBreaching = false;
		this.swellingCreeper.setSwellDir(-1);
		AttributeInstance movementSpeed = this.swellingCreeper.getAttribute(Attributes.MOVEMENT_SPEED);
		if (movementSpeed != null)
			movementSpeed.removeModifier(WALKING_FUSE_SPEED_MODIFIER_UUID);
		this.angle = 0;
	}

	public void tick() {
		if (this.creeperAttackTarget == null || !this.creeperAttackTarget.isAlive())
			this.tryCancelSwell();
		if (this.isBreaching && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) >= 14 * 14)
			this.tryCancelSwell();
		else if (this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) > (explosionSizeSqr * 2d * 2d) && !isBreaching)
			this.tryCancelSwell();
		else if (!this.swellingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget) && !ignoreWalls && !isBreaching)
			this.tryCancelSwell();
		else {
			if (this.swellingCreeper.tickCount % 5 == 0) {
				this.swellingCreeper.setSwellDir(1);
				alertNearby();
			}
			this.swellingCreeper.lookAt(this.creeperAttackTarget, 30f, 30f);
			if (this.beta && !this.walkingFuse && this.swellingCreeper.onGround()) {
				Vec3 mov = new Vec3(this.swellingCreeper.getDeltaMovement().x + Math.cos(Math.toRadians(angle)) * (this.explosionSize / 50f), this.swellingCreeper.getDeltaMovement().y, this.swellingCreeper.getDeltaMovement().z + Math.sin(Math.toRadians(angle)) * (this.explosionSize / 50f));
				this.swellingCreeper.setDeltaMovement(mov);
				Direction direction = Direction.fromYRot(angle - 90);
				BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(this.swellingCreeper.getX() + mov.x, this.swellingCreeper.getY(), this.swellingCreeper.getZ() + mov.z).move(direction);
				if (this.swellingCreeper.level().getBlockState(blockPos).isSolid())
					this.swellingCreeper.getJumpControl().jump();
                if (this.betaStrafeLeft)
                    angle += (1f / this.explosionSize) * 12;
                else
                    angle -= (1f / this.explosionSize) * 12;
            }
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
					avoidExplosionGoal.run(this.swellingCreeper, explosionSize);
				}
			});
		}
	}

	private void tryCancelSwell() {
		if (!this.forceExplode)
			this.swellingCreeper.setSwellDir(-1);
	}

	public EACreeperSwellGoal setIgnoreWalls(boolean ignoreWalls) {
		this.ignoreWalls = ignoreWalls;
		return this;
	}

	public EACreeperSwellGoal setWalkingFuse(boolean walkingFuse) {
		if (walkingFuse)
			this.setFlags(EnumSet.noneOf(Goal.Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.MOVE));

		this.walkingFuse = walkingFuse;
		return this;
	}

	public EACreeperSwellGoal setBreaching(boolean breaching) {
		this.breaching = breaching;
		return this;
	}

	public EACreeperSwellGoal setForceExplode(boolean forceExplode) {
		this.forceExplode = forceExplode;
		return this;
	}

	public EACreeperSwellGoal setBeta(boolean beta) {
		this.beta = beta;
		if (beta)
			this.betaStrafeLeft = this.swellingCreeper.getRandom().nextBoolean();
		return this;
	}

	public static boolean canBreach(Creeper creeper, LivingEntity target) {
		if (!creeper.getPersistentData().contains(CreeperSwell.BREACH))
			return false;
		double yDistance = creeper.getY() - target.getY();
		double x = target.getX() - creeper.getX();
		double z = target.getZ() - creeper.getZ();
		double xzDistance = x * x + z * z;
		return (creeper.getNavigation().isDone() || creeper.getNavigation().isStuck())
				&& !creeper.getSensing().hasLineOfSight(target)
				&& !creeper.isInWater()
				&& xzDistance < (CreeperUtils.getExplosionSizeSqr(creeper) * 5d * 5d)
				&& yDistance > -CreeperUtils.getExplosionSize(creeper) - 2;
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}