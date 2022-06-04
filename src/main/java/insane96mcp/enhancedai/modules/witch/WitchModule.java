package insane96mcp.enhancedai.modules.witch;

import insane96mcp.enhancedai.modules.witch.feature.ThirstyWitches;
import insane96mcp.enhancedai.modules.witch.feature.WitchFleeTarget;
import insane96mcp.enhancedai.modules.witch.feature.WitchPotionThrowing;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Witch")
public class WitchModule extends Module {

	public WitchPotionThrowing witchPotionThrowing;
	public WitchFleeTarget witchFleeTarget;
	public ThirstyWitches thirstyWitches;

	public WitchModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		witchPotionThrowing = new WitchPotionThrowing(this);
		witchFleeTarget = new WitchFleeTarget(this);
		thirstyWitches = new ThirstyWitches(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		witchPotionThrowing.loadConfig();
		witchFleeTarget.loadConfig();
		thirstyWitches.loadConfig();
	}
}
