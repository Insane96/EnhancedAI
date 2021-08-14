package insane96mcp.enhancedai.modules.base;


import insane96mcp.enhancedai.modules.base.feature.AvoidExplosionsFeature;
import insane96mcp.enhancedai.modules.base.feature.BaseFeature;
import insane96mcp.enhancedai.modules.base.feature.TargetingFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Base")
public class BaseModule extends Module {

	BaseFeature baseFeature;
	TargetingFeature targetingFeature;
	AvoidExplosionsFeature avoidExplosionsFeature;

	public BaseModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		baseFeature = new BaseFeature(this);
		targetingFeature = new TargetingFeature(this);
		avoidExplosionsFeature = new AvoidExplosionsFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		baseFeature.loadConfig();
		targetingFeature.loadConfig();
		avoidExplosionsFeature.loadConfig();
	}
}
