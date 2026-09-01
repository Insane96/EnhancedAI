package insane96mcp.enhancedai.module.mobs.sprint;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class SprintGoal extends Goal {
	protected Mob goalOwner;
	protected LivingEntity target;

	/// When above 0, the ticks the mob is able to sprint for, when below, the cooldown
	public int sprintTicks;

	public SprintGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
	}

	@Override
	public boolean canUse() {
		if (this.sprintTicks < 0) {
			++this.sprintTicks;
			return false;
		}
		this.target = this.goalOwner.getTarget();
		if (this.target == null)
			return false;
		return this.goalOwner.distanceToSqr(this.target) < 100;
	}

	@Override
	public boolean canContinueToUse() {
		return super.canContinueToUse() && this.sprintTicks > 0 && this.goalOwner.distanceToSqr(this.target) > 4;
	}

	@Override
	public void start() {
		this.goalOwner.setSprinting(true);
		this.sprintTicks = 100;
	}

	@Override
	public void tick() {
		--this.sprintTicks;
	}

	@Override
	public void stop() {
		this.goalOwner.setSprinting(false);
		this.sprintTicks = -100 + this.sprintTicks;
	}
}
