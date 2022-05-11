package insane96mcp.enhancedai.modules.base;


import insane96mcp.enhancedai.modules.base.feature.AvoidExplosions;
import insane96mcp.enhancedai.modules.base.feature.Base;
import insane96mcp.enhancedai.modules.base.feature.Targeting;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Base")
public class BaseModule extends Module {

	public Base base;
	public Targeting targeting;
	public AvoidExplosions avoidExplosions;

	public BaseModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		base = new Base(this);
		targeting = new Targeting(this);
		avoidExplosions = new AvoidExplosions(this);
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
