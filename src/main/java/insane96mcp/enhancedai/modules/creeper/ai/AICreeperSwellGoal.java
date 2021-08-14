package insane96mcp.enhancedai.modules.creeper.ai;

import insane96mcp.enhancedai.modules.base.ai.AIAvoidExplosionGoal;
import insane96mcp.enhancedai.setup.ModSounds;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.EnumSet;
import java.util.List;

public class AICreeperSwellGoal extends Goal {

	protected final CreeperEntity swellingCreeper;
	private LivingEntity creeperAttackTarget;

	private boolean isCena = false;
	private boolean walkingFuse = false;
	private boolean ignoreWalls = false;
	private boolean breaching = false;
	private double explosionSize = -1;

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
		if (this.explosionSize < 0) {
			CompoundNBT compoundNBT = new CompoundNBT();
			this.swellingCreeper.writeAdditional(compoundNBT);
			this.explosionSize = compoundNBT.getByte("ExplosionRadius");
			this.explosionSize *= compoundNBT.getBoolean("powered") ? 2 : 1;
		}

		return this.swellingCreeper.getCreeperState() > 0 ||
				ignoreWalls && target != null && Math.sqrt(this.swellingCreeper.getDistanceSq(target)) < this.explosionSize * 1.5d ||
				target != null && this.swellingCreeper.getDistance(target) < this.explosionSize;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		if (!walkingFuse)
			this.swellingCreeper.getNavigator().clearPath();
		this.creeperAttackTarget = this.swellingCreeper.getAttackTarget();
		if (isCena)
			this.swellingCreeper.playSound(ModSounds.CREEPER_CENA_FUSE.get(), 4.0f, 1.0f);
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.creeperAttackTarget = null;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		if (this.creeperAttackTarget == null)
			this.swellingCreeper.setCreeperState(-1);
		//else if (this.swellingCreeper.getNavigator().getPath() != null && this.swellingCreeper.getNavigator().getPath().isFinished())
			//this.swellingCreeper.setCreeperState(1);
		else if (this.swellingCreeper.getDistance(this.creeperAttackTarget) > this.explosionSize * 2)
			this.swellingCreeper.setCreeperState(-1);
		else if (!this.swellingCreeper.getEntitySenses().canSee(this.creeperAttackTarget) && !ignoreWalls)
			this.swellingCreeper.setCreeperState(-1);
		else {
			this.swellingCreeper.setCreeperState(1);
			List<CreatureEntity> creaturesNearby = this.swellingCreeper.world.getEntitiesWithinAABB(CreatureEntity.class, this.swellingCreeper.getBoundingBox().grow(this.explosionSize * 2));
			for (CreatureEntity creatureEntity : creaturesNearby) {
				if (creatureEntity == this.swellingCreeper)
					continue;
				creatureEntity.goalSelector.goals.forEach(prioritizedGoal -> {
					if (prioritizedGoal.getGoal() instanceof AIAvoidExplosionGoal) {
						AIAvoidExplosionGoal aiAvoidExplosionGoal = (AIAvoidExplosionGoal) prioritizedGoal.getGoal();
						aiAvoidExplosionGoal.run(this.swellingCreeper, this.explosionSize);
					}
				});
			}
		}
	}

	public void setCena(boolean isCena) {
		this.isCena = isCena;
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