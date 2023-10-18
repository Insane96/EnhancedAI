package insane96mcp.enhancedai.modules.base.movement;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ClimbLaddersGoal extends Goal {
	protected LivingEntity target;
	protected Mob goalOwner;

	public ClimbLaddersGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		this.target = this.goalOwner.getTarget();
		return this.goalOwner.onClimbable() && this.target != null;
	}

	@Override
	public void tick() {
		double targetY = this.target.getY();
		double yMotion = 0;
		double distanceX = this.target.getX() - this.goalOwner.getX();
		double distanceZ = this.target.getZ() - this.goalOwner.getZ();
		Vec3 vec3 = new Vec3(distanceX, 0, distanceZ).normalize();
		if (Mth.floor(targetY) == Mth.floor(this.goalOwner.getY() + 0.5f)) {
			this.goalOwner.getNavigation().moveTo(this.target, 4d);
			this.goalOwner.setDeltaMovement(this.goalOwner.getDeltaMovement().add(vec3.x, 0.25d, vec3.z));
			//this.goalOwner.getJumpControl().jump();
		}
		else if (targetY > this.goalOwner.getY())
			yMotion = 0.16;
		this.goalOwner.lookAt(this.target, 30, 30);
		this.goalOwner.setDeltaMovement(this.goalOwner.getDeltaMovement().multiply(0.1, 1, 0.1));
		this.goalOwner.setDeltaMovement(this.goalOwner.getDeltaMovement().add(0, yMotion, 0));
		if (this.goalOwner.tickCount % 5 == 0)
			this.goalOwner.playSound(SoundEvents.LADDER_STEP);
	}
}
