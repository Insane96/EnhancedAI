package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.modules.witch.ai.DarkArtWitchGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Dark Arts Witches", description = "Witches summon Villagers and cast a lightning bolt upon them.")
public class DarkArtWitch extends Feature {

    private final ForgeConfigSpec.DoubleValue darkArtChanceConfig;

    public double darkArtChance = 0.1d;

    public DarkArtWitch(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        this.darkArtChanceConfig = Config.builder
                .comment("Chance for a witch to get the Dark Art AI (as soon as they have a target and are less than 10 blocks away from the target will summon a Villager and cast a lightning bolt on them")
                .defineInRange("Dark Art Chance", this.darkArtChance, 0d, 1d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.darkArtChance = this.darkArtChanceConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (event.getWorld().isClientSide)
            return;

        if (!(event.getEntity() instanceof Witch witch))
            return;

        boolean darkArt = witch.level.random.nextDouble() < this.darkArtChance;

        if (!darkArt)
            return;

        DarkArtWitchGoal darkArtWitchGoal = new DarkArtWitchGoal(witch);
        witch.goalSelector.addGoal(1, darkArtWitchGoal);
    }
}
