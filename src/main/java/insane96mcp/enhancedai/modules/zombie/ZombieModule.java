package insane96mcp.enhancedai.modules.zombie;

import insane96mcp.enhancedai.modules.zombie.feature.DiggerZombie;
import insane96mcp.enhancedai.modules.zombie.feature.PearlerZombie;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Zombie")
public class ZombieModule extends Module {

	public DiggerZombie diggerZombie;
	public PearlerZombie pearlerZombie;
	//public FisherZombie fisherZombie;

	public ZombieModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		this.diggerZombie = new DiggerZombie(this);
		this.pearlerZombie = new PearlerZombie(this);
		//this.fisherZombie = new FisherZombie(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.diggerZombie.loadConfig();
		this.pearlerZombie.loadConfig();
		//this.fisherZombie.loadConfig();
	}
}
