package insane96mcp.enhancedai.modules.base.movement;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class ClimbLaddersGoal extends Goal {
	protected LivingEntity target;
	protected Mob goalOwner;

	public ClimbLaddersGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
	}

	@Override
	public boolean canUse() {
		this.target = this.goalOwner.getTarget();
		return this.goalOwner.onClimbable() && this.target != null;
	}

	@Override
	public void tick() {
		double targetY = this.target.getY();
		double yMotion;
		if (targetY <= this.goalOwner.getY())
			yMotion = -0.16;
		else
			yMotion = 0.16;
		this.goalOwner.setDeltaMovement(this.goalOwner.getDeltaMovement().multiply(0.1, 1, 0.1));
		this.goalOwner.setDeltaMovement(this.goalOwner.getDeltaMovement().add(0, yMotion, 0));
	}
}
