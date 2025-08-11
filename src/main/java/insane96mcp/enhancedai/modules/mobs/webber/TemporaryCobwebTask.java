package insane96mcp.enhancedai.modules.mobs.webber;

import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TemporaryCobwebTask extends ScheduledTickTask {

	Level level;
	BlockPos pos;

	public TemporaryCobwebTask(int tickDelay, Level level, BlockPos pos) {
		super(tickDelay);
		this.level = level;
		this.pos = pos;
	}

	@Override
	public void run() {
        if (this.level.getBlockState(pos).is(Blocks.COBWEB))
		    this.level.destroyBlock(pos, false);
	}
}
