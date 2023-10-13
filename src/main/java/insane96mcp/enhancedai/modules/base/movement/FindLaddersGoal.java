package insane96mcp.enhancedai.modules.base.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FindLaddersGoal extends Goal {
	protected LivingEntity target;
	protected Mob goalOwner;

	public FindLaddersGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		this.target = this.goalOwner.getTarget();
		return this.goalOwner.getNavigation().isDone() && this.target != null;
	}

	@Override
	public void start() {
		Vec3 vec3;
		if (this.target.getY() - this.goalOwner.getY() > 0) {
			vec3 = this.findClimbable();
			if (vec3 == null)
				return;
			this.goalOwner.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1);
		}
	}

	public Vec3 findClimbable() {
		for (int i = 0; i < 100; i++) {
			//Find a climbable block in a 16 block radius
			int randX = this.goalOwner.getBlockX() + Mth.nextInt(this.goalOwner.getRandom(), -16, 16);
			int randY = this.goalOwner.getBlockY() + Mth.nextInt(this.goalOwner.getRandom(), -16, 16);
			int randZ = this.goalOwner.getBlockZ() + Mth.nextInt(this.goalOwner.getRandom(), -16, 16);
			BlockPos pos = new BlockPos(randX, randY, randZ);
			if (this.goalOwner.level().getBlockState(pos).isLadder(this.goalOwner.level(), pos, this.goalOwner))
				return new Vec3(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
		}
		return null;
	}
}
