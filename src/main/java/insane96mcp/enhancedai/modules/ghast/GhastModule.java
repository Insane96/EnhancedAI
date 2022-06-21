package insane96mcp.enhancedai.modules.ghast;

import insane96mcp.enhancedai.modules.ghast.feature.GhastShoot;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Ghast")
public class GhastModule extends Module {

    public GhastShoot ghastShoot;

    public GhastModule() {
        super(Config.builder);
        this.pushConfig(Config.builder);
        ghastShoot = new GhastShoot(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        ghastShoot.loadConfig();
    }
}
