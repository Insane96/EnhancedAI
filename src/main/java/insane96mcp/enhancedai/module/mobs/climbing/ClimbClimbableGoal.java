package insane96mcp.enhancedai.module.mobs.climbing;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.EnumSet;
import java.util.Optional;

public class ClimbClimbableGoal extends Goal {
	protected LivingEntity target;
	protected Mob goalOwner;
	//Same distance between step sounds as vanilla (see Entity#move)
	private static final double BLOCKS_PER_STEP_SOUND = 1d / 0.6d;
	private double lastY;
	private double climbedDistance;

	public ClimbClimbableGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
		//No MOVE flag: climbing only changes the delta movement, so it has to run alongside melee attack and mining goals
		this.setFlags(EnumSet.of(Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		this.target = this.goalOwner.getTarget();
		return this.goalOwner.onClimbable() && this.target != null;
	}

	@Override
	public void start() {
		this.lastY = this.goalOwner.getY();
		this.climbedDistance = 0d;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		double targetY = this.target.getY();
		Vec3 motion = this.goalOwner.getDeltaMovement();
		double xMotion = motion.x * 0.1d;
		double yMotion = motion.y;
		double zMotion = motion.z * 0.1d;
		double distanceX = this.target.getX() - this.goalOwner.getX();
		double distanceZ = this.target.getZ() - this.goalOwner.getZ();
		Vec3 vec3 = new Vec3(distanceX, 0, distanceZ).normalize();
		if (Mth.floor(targetY) == Mth.floor(this.goalOwner.getY() + 0.5f)) {
			this.goalOwner.getNavigation().moveTo(this.target, 4d);
			xMotion = (motion.x + vec3.x) * 0.1d;
			yMotion = 0.25d;
			zMotion = (motion.z + vec3.z) * 0.1d;
			//this.goalOwner.getJumpControl().jump();
		}
		else if (targetY > this.goalOwner.getY()) {
			yMotion = 0.12d;
			//Keeps the mob centered on the climbable, so its hitbox doesn't get caught under blocks in the adjacent columns
			Vec3 center = Vec3.atCenterOf(this.goalOwner.blockPosition());
			xMotion = (center.x - this.goalOwner.getX()) * 0.25d;
			zMotion = (center.z - this.goalOwner.getZ()) * 0.25d;
		}
		this.goalOwner.lookAt(this.target, 30, 30);
		this.goalOwner.setDeltaMovement(xMotion, yMotion, zMotion);
		this.climbedDistance += Math.abs(this.goalOwner.getY() - this.lastY);
		this.lastY = this.goalOwner.getY();
		if (this.climbedDistance >= BLOCKS_PER_STEP_SOUND) {
			this.climbedDistance -= BLOCKS_PER_STEP_SOUND;
			BlockPos blockpos = this.goalOwner.blockPosition();
			BlockState blockstate = this.goalOwner.getBlockStateOn();
			Optional<BlockPos> ladderPos = CommonHooks.isLivingOnLadder(blockstate, this.goalOwner.level(), blockpos, this.goalOwner);
			if (ladderPos.isPresent()) {
				SoundType soundType = blockstate.getBlock().getSoundType(blockstate, this.goalOwner.level(), blockpos, this.goalOwner);
				this.goalOwner.playSound(soundType.getStepSound(), soundType.getVolume() * 0.15f, soundType.getPitch());
			}
		}
	}
}
