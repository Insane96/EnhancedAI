package insane96mcp.enhancedai.module.mobs.climbing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Vanilla pathfinding doesn't know about climbable blocks, so a mob stops under a target it can't reach. This walks it to a nearby climbable, then {@link ClimbClimbableGoal} takes over.
 */
public class MoveToClimbableGoal extends Goal {
	//Must be even: goalSelector only checks canUse when (tickCount + id) is even (see Mob#serverAiStep)
	private static final int CHECK_INTERVAL = 40;
	private static final int VERTICAL_RADIUS = 3;
	private static final int MIN_TARGET_HEIGHT = 2;
	private static final int MIN_COLUMN_HEIGHT = 2;
	private static final int MAX_PATHS_PER_CHECK = 4;
	private static final int TIMEOUT = 200;
	//The melee goal never ends and brings the mob back under the target, so the same climbable must not be tried again right away
	private static final int SAME_COLUMN_COOLDOWN = 400;
	private static final double SPEED_MODIFIER = 1d;

	private final Mob goalOwner;
	private LivingEntity target;
	private Path path;
	private BlockPos climbablePos;
	private int timeoutTick;
	private BlockPos lastColumn;
	private int lastColumnCooldownTick;

	public MoveToClimbableGoal(Mob goalOwner) {
		super();
		this.goalOwner = goalOwner;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if ((this.goalOwner.tickCount + this.goalOwner.getId()) % CHECK_INTERVAL != 0)
			return false;

		LivingEntity currentTarget = this.goalOwner.getTarget();
		if (currentTarget == null
				|| !currentTarget.isAlive()
				|| !this.goalOwner.onGround()
				|| this.goalOwner.onClimbable()
				|| currentTarget.getY() - this.goalOwner.getY() < MIN_TARGET_HEIGHT)
			return false;

		//Scanning blocks is cheaper than pathfinding and most of the times there's no climbable nearby
		List<BlockPos> climbables = this.findClimbables();
		if (climbables.isEmpty())
			return false;

		PathNavigation navigation = this.goalOwner.getNavigation();
		Path targetPath = navigation.createPath(currentTarget, 0);
		if (targetPath != null && targetPath.canReach())
			return false;

		for (int i = 0; i < climbables.size() && i < MAX_PATHS_PER_CHECK; i++) {
			BlockPos climbable = climbables.get(i);
			Path climbablePath = navigation.createPath(climbable, 0);
			if (climbablePath != null && climbablePath.canReach()) {
				this.target = currentTarget;
				this.path = climbablePath;
				this.climbablePos = climbable;
				return true;
			}
		}

		//createPath stores its destination in the navigation even without moveTo, and recomputePath would reuse the last discarded climbable
		navigation.createPath(currentTarget, 0);
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.goalOwner.getTarget() == this.target
				&& this.target.isAlive()
				&& !this.goalOwner.onClimbable()
				&& !this.goalOwner.getNavigation().isDone()
				&& this.goalOwner.tickCount < this.timeoutTick;
	}

	@Override
	public void start() {
		this.goalOwner.getNavigation().moveTo(this.path, SPEED_MODIFIER);
		this.timeoutTick = this.goalOwner.tickCount + TIMEOUT;
	}

	@Override
	public void stop() {
		this.lastColumn = this.climbablePos;
		this.lastColumnCooldownTick = this.goalOwner.tickCount + SAME_COLUMN_COOLDOWN;
		this.goalOwner.getNavigation().stop();
		this.target = null;
		this.path = null;
		this.climbablePos = null;
	}

	/**
	 * Returns the climbables the mob can stand in that rise above it, from the nearest. A climbable with no floor can never be the end of a complete path.
	 */
	private List<BlockPos> findClimbables() {
		Level level = this.goalOwner.level();
		BlockPos origin = this.goalOwner.blockPosition();
		int radius = Climbing.CLIMBABLE_SEARCH_RADIUS.get(this.goalOwner);
		int minColumnTop = origin.getY() + MIN_COLUMN_HEIGHT;
		List<BlockPos> climbables = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -VERTICAL_RADIUS, -radius), origin.offset(radius, VERTICAL_RADIUS, radius))) {
			if (!level.getBlockState(pos).is(BlockTags.CLIMBABLE) || this.isColumnOnCooldown(pos))
				continue;
			BlockPos below = pos.below();
			BlockState belowState = level.getBlockState(below);
			if (belowState.is(BlockTags.CLIMBABLE) || belowState.getCollisionShape(level, below).isEmpty())
				continue;
			if (this.getColumnTop(pos) >= minColumnTop)
				climbables.add(pos.immutable());
		}
		climbables.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(this.goalOwner.position())));
		return climbables;
	}

	private boolean isColumnOnCooldown(BlockPos pos) {
		return this.lastColumn != null
				&& this.goalOwner.tickCount < this.lastColumnCooldownTick
				&& this.lastColumn.getX() == pos.getX()
				&& this.lastColumn.getZ() == pos.getZ();
	}

	private int getColumnTop(BlockPos base) {
		Level level = this.goalOwner.level();
		BlockPos.MutableBlockPos cursor = base.mutable();
		int top = base.getY();
		while (level.getBlockState(cursor.move(Direction.UP)).is(BlockTags.CLIMBABLE))
			top = cursor.getY();
		return top;
	}
}
