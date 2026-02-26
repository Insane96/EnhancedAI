package insane96mcp.enhancedai.module.bugs.silverfish.wakeupfriends;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;

public class EAISilverfishWakeUpFriendsGoal extends Silverfish.SilverfishWakeUpFriendsGoal {
	private int lookForFriends;
	private final Silverfish silverfish;

	public EAISilverfishWakeUpFriendsGoal(Silverfish pSilverfish) {
		super(pSilverfish);
		this.silverfish = pSilverfish;
	}

	@Override
	public void notifyHurt() {
		if (this.lookForFriends == 0)
			this.lookForFriends = this.adjustedTickDelay(SilverfishWakeUpFriends.TICKS_AFTER_HURT_TO_WAKE_UP_FRIENDS.get(this.silverfish));
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
		int verticalRange = SilverfishWakeUpFriends.VERTICAL_WAKE_UP_RANGE.get(this.silverfish);
		int horizontalRange = SilverfishWakeUpFriends.HORIZONTAL_WAKE_UP_RANGE.get(this.silverfish);
		for (int i = 0; i <= verticalRange && i >= -verticalRange; i = (i <= 0 ? 1 : 0) - i) {
			for (int j = 0; j <= horizontalRange && j >= -horizontalRange; j = (j <= 0 ? 1 : 0) - j) {
				for (int k = 0; k <= horizontalRange && k >= -horizontalRange; k = (k <= 0 ? 1 : 0) - k) {
					BlockPos offsetBlockPos = blockPos.offset(j, i, k);
					BlockState offsetBlockState = level.getBlockState(offsetBlockPos);
					Block offsetBlock = offsetBlockState.getBlock();
					if (!(offsetBlock instanceof InfestedBlock))
						continue;
					if (EventHooks.canEntityGrief(level, this.silverfish))
						level.destroyBlock(offsetBlockPos, true, this.silverfish);
					else
						level.setBlock(offsetBlockPos, ((InfestedBlock) offsetBlock).hostStateByInfested(level.getBlockState(offsetBlockPos)), 3);
					if (rng.nextInt(SilverfishWakeUpFriends.CHANCE_TO_STOP_WAKING_UP_FRIENDS.get(this.silverfish)) == 0)
						return;
				}
			}
		}
	}
}
