package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.modules.villager.feature.VillagerAttacking;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Villager")
public class VillagerModule extends Module {

    public VillagerAttacking villagerAttacking;

    public VillagerModule() {
        super(Config.builder);
        this.pushConfig(Config.builder);
        villagerAttacking = new VillagerAttacking(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        villagerAttacking.loadConfig();
    }
}
