package insane96mcp.enhancedai.modules.base;


import insane96mcp.enhancedai.modules.base.feature.AvoidExplosionsFeature;
import insane96mcp.enhancedai.modules.base.feature.BaseFeature;
import insane96mcp.enhancedai.modules.base.feature.TargetingFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Base")
public class BaseModule extends Module {

	BaseFeature base;
	TargetingFeature targeting;
	AvoidExplosionsFeature avoidExplosions;

	public BaseModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		base = new BaseFeature(this);
		targeting = new TargetingFeature(this);
		avoidExplosions = new AvoidExplosionsFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		base.loadConfig();
		targeting.loadConfig();
		avoidExplosions.loadConfig();
	}
}
