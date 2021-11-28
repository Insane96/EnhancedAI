package insane96mcp.enhancedai.utils;

public abstract class ScheduledTickTask implements Runnable {
	int tickDelay;
	int tick;

	boolean executed = false;

	public ScheduledTickTask(int tickDelay) {
		this.tickDelay = tickDelay;
		this.tick = 0;
	}

	public final void tick() {
		if (this.tick++ >= this.tickDelay) {
			this.run();
			this.executed = true;
		}
	}

	@Override
	public abstract void run();

	public boolean hasBeenExecuted() {
		return this.executed;
	}
}