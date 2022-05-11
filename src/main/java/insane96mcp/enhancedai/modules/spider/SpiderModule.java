package insane96mcp.enhancedai.modules.spider;

import insane96mcp.enhancedai.modules.spider.feature.Misc;
import insane96mcp.enhancedai.modules.spider.feature.ThrowingWeb;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Spider")
public class SpiderModule extends Module {

	public ThrowingWeb throwingWeb;
	public Misc misc;

	public SpiderModule() {
		super(Config.builder);
		this.pushConfig(Config.builder);
		this.throwingWeb = new ThrowingWeb(this);
		this.misc = new Misc(this);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		throwingWeb.loadConfig();
		misc.loadConfig();
	}
}
