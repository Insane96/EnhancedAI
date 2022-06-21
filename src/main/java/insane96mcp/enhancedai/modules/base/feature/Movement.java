package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.ClimbLaddersGoal;
import insane96mcp.enhancedai.modules.base.ai.FindLaddersGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Movement", description = "Makes mobs be able to move in more ways, like climbing.")
public class Movement extends Feature {

    private final ForgeConfigSpec.ConfigValue<Boolean> allowClimbingConfig;
    private final ForgeConfigSpec.ConfigValue<Boolean> targetLaddersConfig;

    public boolean allowClimbing = false;
    public boolean targetLadders = false;

    public Movement(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        this.allowClimbingConfig = Config.builder
                .comment("If true, mobs will be able to climb (up and down)")
                .define("Allow Climbing", this.allowClimbing);
        this.targetLaddersConfig = Config.builder
                .comment("If true, mobs try to find climbable blocks to reach the target")
                .define("Target Ladders", this.targetLadders);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.allowClimbing = this.allowClimbingConfig.get();
        this.targetLadders = this.targetLaddersConfig.get();
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntity() instanceof Mob mob))
            return;

        if (this.allowClimbing) {
            mob.goalSelector.addGoal(3, new ClimbLaddersGoal(mob));
        }

        if (this.targetLadders) {
            mob.targetSelector.addGoal(0, new FindLaddersGoal(mob));
        }
    }
}