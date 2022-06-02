package insane96mcp.enhancedai.modules.witch;

import insane96mcp.enhancedai.modules.witch.feature.WitchFleeTarget;
import insane96mcp.enhancedai.modules.witch.feature.WitchPotionThrowing;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Witch")
public class WitchModule extends Module {

	public WitchPotionThrowing witchPotionThrowing;
	public WitchFleeTarget witchFleeTarget;

	public WitchModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		witchPotionThrowing = new WitchPotionThrowing(this);
		witchFleeTarget = new WitchFleeTarget(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		witchPotionThrowing.loadConfig();
		witchFleeTarget.loadConfig();
	}
}
