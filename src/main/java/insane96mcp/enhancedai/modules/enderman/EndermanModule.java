package insane96mcp.enhancedai.modules.enderman;

import insane96mcp.enhancedai.modules.enderman.feature.GetOverHere;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Enderman")
public class EndermanModule extends Module {

	public GetOverHere getOverHere;
	//public ItemDisruption itemDisruption;

	public EndermanModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		this.getOverHere = new GetOverHere(this);
		//this.itemDisruption = new ItemDisruption(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.getOverHere.loadConfig();
		//this.itemDisruption.loadConfig();
	}
}
