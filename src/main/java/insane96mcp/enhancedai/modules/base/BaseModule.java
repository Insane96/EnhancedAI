package insane96mcp.enhancedai.modules.base;


import insane96mcp.enhancedai.modules.base.feature.*;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Base")
public class BaseModule extends Module {

	public Base base;
	public Targeting targeting;
	public AvoidExplosions avoidExplosions;
	public AntiCheese antiCheese;
	public Movement movement;
	//public Shielding shielding;

	public BaseModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		base = new Base(this);
		targeting = new Targeting(this);
		avoidExplosions = new AvoidExplosions(this);
		antiCheese = new AntiCheese(this);
		movement = new Movement(this);
		//shielding = new Shielding(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		base.loadConfig();
		targeting.loadConfig();
		avoidExplosions.loadConfig();
		antiCheese.loadConfig();
		movement.loadConfig();
		//shielding.loadConfig();
	}
}
