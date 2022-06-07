package insane96mcp.enhancedai.modules.enderman;

import insane96mcp.enhancedai.modules.witch.feature.WitchPotionThrowing;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Enderman")
public class EndermanModule extends Module {

	public WitchPotionThrowing witchPotionThrowing;

	public EndermanModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		witchPotionThrowing = new WitchPotionThrowing(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		witchPotionThrowing.loadConfig();
	}
}
