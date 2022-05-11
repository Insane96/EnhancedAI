package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.modules.animal.feature.AnimalAttacking;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;

@Label(name = "Animal")
public class AnimalModule extends Module {

    AnimalAttacking animalAttacking;

    public AnimalModule() {
        super(Config.builder);
        this.pushConfig(Config.builder);
        animalAttacking = new AnimalAttacking(this);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        animalAttacking.loadConfig();
    }
}
