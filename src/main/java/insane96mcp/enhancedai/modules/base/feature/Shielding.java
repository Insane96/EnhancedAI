package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.ShieldingGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Shielding", description = "Makes mobs be able to use shields.")
public class Shielding extends Feature {

    //private final ForgeConfigSpec.ConfigValue<Boolean> allowClimbingConfig;

    //public boolean allowClimbing = true;

    public Shielding(Module module) {
        super(Config.builder, module);
        /*this.pushConfig(Config.builder);
        this.allowClimbingConfig = Config.builder
                .comment("If true, mobs will be able to climb (up and down)")
                .define("Allow Climbing", this.allowClimbing);
        this.targetLaddersConfig = Config.builder
                .comment("If true, mobs try to find climbable blocks to reach the target")
                .define("Target Ladders", this.targetLadders);
        Config.builder.pop();*/
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        //this.allowClimbing = this.allowClimbingConfig.get();
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntity() instanceof Mob mob))
            return;

        mob.goalSelector.addGoal(3, new ShieldingGoal(mob, 1d));
    }
}