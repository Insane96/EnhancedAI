package insane96mcp.enhancedai.modules.creeper.ai;

import insane96mcp.enhancedai.modules.base.ai.AvoidExplosionGoal;
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

	public boolean canUse() {
		this.creeperAttackTarget = this.swellingCreeper.getTarget();
		if (creeperAttackTarget == null)
			return false;

		boolean canBreach = breaching && canBreach(this.swellingCreeper, this.creeperAttackTarget);
		boolean ignoresWalls = ignoreWalls && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) < (CreeperUtils.getExplosionSizeSqr(this.swellingCreeper) * 1d * 1d);

		if (canBreach)
			isBreaching = true;

		return (this.swellingCreeper.getSwellDir() > 0) ||
				ignoresWalls ||
				canBreach ||
				(this.swellingCreeper.getSensing().hasLineOfSight(this.creeperAttackTarget) && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) < CreeperUtils.getExplosionSizeSqr(this.swellingCreeper) * 1.5d * 1.5d);
	}

	public void start() {
		if (!walkingFuse)
			this.swellingCreeper.getNavigation().stop();
	}

	public void stop() {
		this.creeperAttackTarget = null;
		this.isBreaching = false;
		this.swellingCreeper.setSwellDir(-1);
	}

	public void tick() {
		if (this.creeperAttackTarget == null || !this.creeperAttackTarget.isAlive())
			this.swellingCreeper.setSwellDir(-1);
		if (this.isBreaching && this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) >= 14 * 14)
			this.swellingCreeper.setSwellDir(-1);
		else if (this.swellingCreeper.distanceToSqr(this.creeperAttackTarget) > (CreeperUtils.getExplosionSizeSqr(this.swellingCreeper) * 2d * 2d) && !isBreaching)
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
					if (prioritizedGoal.getGoal() instanceof AvoidExplosionGoal avoidExplosionGoal) {
						avoidExplosionGoal.run(this.swellingCreeper, CreeperUtils.getExplosionSize(this.swellingCreeper));
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
				&& xzDistance < (CreeperUtils.getExplosionSizeSqr(creeper) * 5d * 5d)
				&& yDistance > -CreeperUtils.getExplosionSize(creeper) - 2;
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}