package insane96mcp.enhancedai.modules.drowned;

import insane96mcp.enhancedai.modules.drowned.feature.DrownedSwimming;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Drowned")
public class DrownedModule extends Module {

	public DrownedSwimming swimming;

	public DrownedModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		this.swimming = new DrownedSwimming(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.swimming.loadConfig();
	}
}
