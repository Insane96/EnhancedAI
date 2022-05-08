package insane96mcp.enhancedai.modules.creeper.ai;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidExplosionGoal;
import insane96mcp.enhancedai.modules.creeper.utils.CreeperUtils;
import insane96mcp.enhancedai.setup.Strings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;

import java.util.EnumSet;
import java.util.List;

public class AICreeperSwellGoal extends Goal {

	protected final Creeper swellingCreeper;
	private LivingEntity creeperAttackTarget;

	private boolean walkingFuse = false;
	private boolean ignoreWalls = false;
	private boolean breaching = false;

	private boolean isBreaching = false;

	public AICreeperSwellGoal(Creeper entitycreeperIn) {
		this.swellingCreeper = entitycreeperIn;
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		LivingEntity target = this.swellingCreeper.getTarget();
		if (target == null)
			return false;

		boolean canBreach = breaching && canBreach(this.swellingCreeper, target);
		boolean ignoresWalls = ignoreWalls && this.swellingCreeper.distanceToSqr(target) < (CreeperUtils.getExplosionSizeSq(this.swellingCreeper) * 1d * 1d);

		if (canBreach)
			isBreaching = true;

		return (this.swellingCreeper.getSwellDir() > 0) ||
				ignoresWalls ||
				(this.swellingCreeper.getSensing().hasLineOfSight(target) && this.swellingCreeper.distanceToSqr(target) < CreeperUtils.getExplosionSizeSq(this.swellingCreeper) * 1.5d * 1.5d) ||
				canBreach;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		if (!walkingFuse)
			this.swellingCreeper.getNavigation().stop();
		this.creeperAttackTarget = this.swellingCreeper.getTarget();
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.creeperAttackTarget = null;
		this.isBreaching = false;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.creeperAttackTarget == null || !this.creeperAttackTarget.isAlive())
			this.swellingCreeper.setSwellDir(-1);
		if (this.isBreaching && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) >= 14 * 14)
			this.swellingCreeper.setSwellDir(-1);
		else if (this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) > (CreeperUtils.getExplosionSizeSq(this.swellingCreeper) * 2d * 2d) && !isBreaching)
			this.swellingCreeper.setSwellDir(-1);
		else if (!this.swellingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget) && !ignoreWalls && !isBreaching)
			this.swellingCreeper.setSwellDir(-1);
		else {
			this.swellingCreeper.setSwellDir(1);
			List<PathfinderMob> creaturesNearby = this.swellingCreeper.level.getEntitiesOfClass(PathfinderMob.class, this.swellingCreeper.getBoundingBox().inflate(CreeperUtils.getExplosionSize(this.swellingCreeper) * 2));
			for (PathfinderMob creatureEntity : creaturesNearby) {
				if (creatureEntity == this.swellingCreeper)
					continue;
				creatureEntity.goalSelector.availableGoals.forEach(prioritizedGoal -> {
					if (prioritizedGoal.getGoal() instanceof AIAvoidExplosionGoal aiAvoidExplosionGoal) {
						aiAvoidExplosionGoal.run(this.swellingCreeper, CreeperUtils.getExplosionSize(this.swellingCreeper));
					}
				});
			}
		}
	}

	public void setIgnoreWalls(boolean ignoreWalls) {
		this.ignoreWalls = ignoreWalls;
	}

	public void setWalkingFuse(boolean walkingFuse) {
		if (walkingFuse)
			this.setFlags(EnumSet.noneOf(Goal.Flag.class));
		else
			this.setFlags(EnumSet.of(Flag.MOVE));

		this.walkingFuse = walkingFuse;
	}

	public void setBreaching(boolean breaching) {
		this.breaching = breaching;
	}

	public static boolean canBreach(Creeper creeper, LivingEntity target) {
		if (!creeper.getPersistentData().contains(Strings.Tags.Creeper.BREACH))
			return false;
		double yDistance = creeper.getY() - target.getY();
		double x = target.getX() - creeper.getX();
		double z = target.getZ() - creeper.getZ();
		double xzDistance = x * x + z * z;
		return (creeper.getNavigation().isDone() || creeper.getNavigation().isStuck())
				&& !creeper.getSensing().hasLineOfSight(target)
				&& !creeper.isInWater()
				&& xzDistance < (CreeperUtils.getExplosionSizeSq(creeper) * 5d * 5d)
				&& yDistance > -CreeperUtils.getExplosionSize(creeper) - 2;
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}