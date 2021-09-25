package insane96mcp.enhancedai.modules.zombie;

import insane96mcp.enhancedai.modules.zombie.feature.DiggerZombieFeature;
import insane96mcp.enhancedai.modules.zombie.feature.PearlerZombieFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Zombie")
public class ZombieModule extends Module {

	public DiggerZombieFeature diggerZombie;
	public PearlerZombieFeature pearlerZombie;

	public ZombieModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		this.diggerZombie = new DiggerZombieFeature(this);
		this.pearlerZombie = new PearlerZombieFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.diggerZombie.loadConfig();
		this.pearlerZombie.loadConfig();
	}
}
