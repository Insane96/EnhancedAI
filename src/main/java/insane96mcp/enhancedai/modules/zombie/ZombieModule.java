package insane96mcp.enhancedai.modules.zombie;

import insane96mcp.enhancedai.modules.zombie.feature.ZombieAIFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Zombie")
public class ZombieModule extends Module {

	public ZombieAIFeature zombieAI;

	public ZombieModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		zombieAI = new ZombieAIFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		zombieAI.loadConfig();
	}
}
