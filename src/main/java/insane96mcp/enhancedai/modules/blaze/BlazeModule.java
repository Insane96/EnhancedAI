package insane96mcp.enhancedai.modules.blaze;

import insane96mcp.enhancedai.modules.blaze.feature.BlazeAttack;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Blaze")
public class BlazeModule extends Module {

    BlazeAttack blazeAttack;

    public BlazeModule() {
        super(Config.builder);
        this.pushConfig(Config.builder);
        blazeAttack = new BlazeAttack(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        blazeAttack.loadConfig();
    }
}