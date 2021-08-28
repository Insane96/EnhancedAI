package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.modules.creeper.feature.CreeperSwellFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Creeper")
public class CreeperModule extends Module {

	CreeperSwellFeature creeperSwell;

	public CreeperModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		creeperSwell = new CreeperSwellFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		creeperSwell.loadConfig();
	}
}
