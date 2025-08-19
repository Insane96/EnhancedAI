package insane96mcp.enhancedai.modules.bugs.silverfish;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class EAISilverfishMergeWithStoneGoal extends RandomStrollGoal {
	@Nullable
	private Direction selectedDirection;
	private boolean doMerge;
	private int initialCooldown;

	public EAISilverfishMergeWithStoneGoal(Silverfish pSilverfish) {
		super(pSilverfish, 1.0D, 10);
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		this.initialCooldown = this.adjustedTickDelay(30);
	}

	public boolean canUse() {
		if (this.mob.getTarget() != null
				|| !this.mob.getNavigation().isDone())
			return false;

		if (--this.initialCooldown > 0)
			return false;
		RandomSource randomsource = this.mob.getRandom();
		if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.mob.level(), this.mob) && randomsource.nextInt(this.adjustedTickDelay(SilverfishFeature.CHANCE_TO_MERGE_WITH_STONE.get(this.mob))) == 0) {
			this.selectedDirection = Direction.getRandom(randomsource);
			BlockPos blockpos = BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()).relative(this.selectedDirection);
			BlockState blockstate = this.mob.level().getBlockState(blockpos);
			if (InfestedBlock.isCompatibleHostBlock(blockstate)) {
				this.doMerge = true;
				return true;
			}
		}

		this.doMerge = false;
		return super.canUse();
	}

	public boolean canContinueToUse() {
		return !this.doMerge && super.canContinueToUse();
	}

	public void start() {
		if (!this.doMerge) {
			super.start();
		} else {
			LevelAccessor levelaccessor = this.mob.level();
            //noinspection DataFlowIssue - Can't be null as will only start if canUse
            BlockPos blockpos = BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()).relative(this.selectedDirection);
			BlockState blockstate = levelaccessor.getBlockState(blockpos);
			if (InfestedBlock.isCompatibleHostBlock(blockstate)) {
				levelaccessor.setBlock(blockpos, InfestedBlock.infestedStateByHost(blockstate), 3);
				this.mob.spawnAnim();
				this.mob.discard();
			}

		}
	}
}
