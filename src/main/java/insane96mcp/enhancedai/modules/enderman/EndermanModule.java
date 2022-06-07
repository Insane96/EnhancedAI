package insane96mcp.enhancedai.modules.enderman;

import insane96mcp.enhancedai.modules.enderman.feature.GetOverHere;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Enderman")
public class EndermanModule extends Module {

	public GetOverHere getOverHere;

	public EndermanModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		getOverHere = new GetOverHere(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		getOverHere.loadConfig();
	}
}
