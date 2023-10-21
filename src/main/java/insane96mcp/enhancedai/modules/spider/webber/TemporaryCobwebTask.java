package insane96mcp.enhancedai.modules.spider.webber;

import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TemporaryCobwebTask extends ScheduledTickTask {

	Level world;
	BlockPos pos;

	public TemporaryCobwebTask(int tickDelay, Level world, BlockPos pos) {
		super(tickDelay);
		this.world = world;
		this.pos = pos;
	}

	@Override
	public void run() {
		this.world.destroyBlock(pos, false);
	}
}
