package insane96mcp.enhancedai.modules.spider;

import insane96mcp.enhancedai.modules.spider.feature.ThrowingWebFeature;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Spider")
public class SpiderModule extends Module {

	public ThrowingWebFeature throwingWeb;

	public SpiderModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		this.throwingWeb = new ThrowingWebFeature(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		throwingWeb.loadConfig();
	}
}
