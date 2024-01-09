package insane96mcp.enhancedai.modules.bugs.silverfish;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EASilverfishWakeUpFriendsGoal extends Silverfish.SilverfishWakeUpFriendsGoal {
	private int lookForFriends;
	private final Silverfish silverfish;

	public EASilverfishWakeUpFriendsGoal(Silverfish pSilverfish) {
		super(pSilverfish);
		this.silverfish = pSilverfish;
	}

	@Override
	public void notifyHurt() {
		if (this.lookForFriends == 0)
			this.lookForFriends = this.adjustedTickDelay(SilverfishFeature.ticksAfterHurtToWakeUpFriends);
	}

	@Override
	public boolean canUse() {
		return this.lookForFriends > 0;
	}

	@Override
	public void tick() {
		if (--this.lookForFriends > 0)
			return;
		Level level = this.silverfish.level();
		RandomSource rng = this.silverfish.getRandom();
		BlockPos blockPos = this.silverfish.blockPosition();
		for (int i = 0; i <= SilverfishFeature.verticalWakeUpRange && i >= -SilverfishFeature.verticalWakeUpRange; i = (i <= 0 ? 1 : 0) - i) {
			for (int j = 0; j <= SilverfishFeature.horizontalWakeUpRange && j >= -SilverfishFeature.horizontalWakeUpRange; j = (j <= 0 ? 1 : 0) - j) {
				for (int k = 0; k <= SilverfishFeature.horizontalWakeUpRange && k >= -SilverfishFeature.horizontalWakeUpRange; k = (k <= 0 ? 1 : 0) - k) {
					BlockPos offsetBlockPos = blockPos.offset(j, i, k);
					BlockState offsetBlockState = level.getBlockState(offsetBlockPos);
					Block offsetBlock = offsetBlockState.getBlock();
					if (!(offsetBlock instanceof InfestedBlock))
						continue;
					if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this.silverfish))
						level.destroyBlock(offsetBlockPos, true, this.silverfish);
					else
						level.setBlock(offsetBlockPos, ((InfestedBlock) offsetBlock).hostStateByInfested(level.getBlockState(offsetBlockPos)), 3);
					if (rng.nextInt(SilverfishFeature.chanceToStopWakingUpFriends) == 0)
						return;
				}
			}
		}
	}
}
