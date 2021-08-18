package insane96mcp.enhancedai.modules.creeper.ai;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidExplosionGoal;
import insane96mcp.enhancedai.modules.creeper.utils.CreeperUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;

import java.util.EnumSet;
import java.util.List;

public class AICreeperSwellGoal extends Goal {

	protected final CreeperEntity swellingCreeper;
	private LivingEntity creeperAttackTarget;

	private boolean walkingFuse = false;
	private boolean ignoreWalls = false;
	private boolean breaching = false;

	/**
	 * When true the creeper has started an explosion that can't be stopped unless the target dies
	 */
	private boolean forceExplosion = false;

	public AICreeperSwellGoal(CreeperEntity entitycreeperIn) {
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
		boolean canBreach = breaching && this.swellingCreeper.getNavigator().noPath() && !this.swellingCreeper.getEntitySenses().canSee(target) && this.swellingCreeper.getDistanceSq(target) < 12 * 12 && yDistance > -CreeperUtils.getExplosionSize(this.swellingCreeper) - 2;
		boolean ignoresWalls = ignoreWalls && this.swellingCreeper.getDistanceSq(target) < (CreeperUtils.getExplosionSizeSq(this.swellingCreeper) * 1.5d * 1.5d);

		if (canBreach)
			forceExplosion = true;

		return (this.swellingCreeper.getCreeperState() > 0) ||
				ignoresWalls ||
				(this.swellingCreeper.getEntitySenses().canSee(target) && this.swellingCreeper.getDistanceSq(target) < CreeperUtils.getExplosionSizeSq(this.swellingCreeper) * 1.5d * 1.5d) ||
				canBreach;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		if (!walkingFuse)
			this.swellingCreeper.getNavigator().clearPath();
		this.creeperAttackTarget = this.swellingCreeper.getAttackTarget();
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.creeperAttackTarget = null;
		this.forceExplosion = false;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.creeperAttackTarget == null || this.creeperAttackTarget.dead)
			this.swellingCreeper.setCreeperState(-1);
		//else if (this.swellingCreeper.getNavigator().getPath() != null && this.swellingCreeper.getNavigator().getPath().isFinished())
			//this.swellingCreeper.setCreeperState(1);
		else if (this.swellingCreeper.getDistanceSq(this.creeperAttackTarget) > (CreeperUtils.getExplosionSizeSq(this.swellingCreeper) * 2d * 2d) && !forceExplosion)
			this.swellingCreeper.setCreeperState(-1);
		else if (!this.swellingCreeper.getEntitySenses().canSee(this.creeperAttackTarget) && !ignoreWalls && !forceExplosion)
			this.swellingCreeper.setCreeperState(-1);
		else {
			this.swellingCreeper.setCreeperState(1);
			List<CreatureEntity> creaturesNearby = this.swellingCreeper.world.getEntitiesWithinAABB(CreatureEntity.class, this.swellingCreeper.getBoundingBox().grow(CreeperUtils.getExplosionSize(this.swellingCreeper) * 2));
			for (CreatureEntity creatureEntity : creaturesNearby) {
				if (creatureEntity == this.swellingCreeper)
					continue;
				creatureEntity.goalSelector.goals.forEach(prioritizedGoal -> {
					if (prioritizedGoal.getGoal() instanceof AIAvoidExplosionGoal) {
						AIAvoidExplosionGoal aiAvoidExplosionGoal = (AIAvoidExplosionGoal) prioritizedGoal.getGoal();
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
			this.setMutexFlags(EnumSet.noneOf(Goal.Flag.class));
		else
			this.setMutexFlags(EnumSet.of(Flag.MOVE));

		this.walkingFuse = walkingFuse;
	}

	public void setBreaching(boolean breaching) {
		this.breaching = breaching;
	}
}