package insane96mcp.enhancedai.modules.spider.entity.projectile;

import insane96mcp.insanelib.utils.scheduled.ScheduledTickTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TemporaryCobwebTask extends ScheduledTickTask {

	World world;
	BlockPos pos;

	public TemporaryCobwebTask(int tickDelay, World world, BlockPos pos) {
		super(tickDelay);
		this.world = world;
		this.pos = pos;
	}

	@Override
	public void run() {
		this.world.destroyBlock(pos, false);
	}
}
