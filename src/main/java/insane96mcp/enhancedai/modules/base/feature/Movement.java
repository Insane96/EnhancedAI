package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.ClimbLaddersGoal;
import insane96mcp.enhancedai.modules.base.ai.FindLaddersGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Movement", description = "Makes mobs be able to move in more ways, like climbing or swim.")
public class Movement extends Feature {

    private final ForgeConfigSpec.ConfigValue<Boolean> allowClimbingConfig;
    private final ForgeConfigSpec.ConfigValue<Boolean> targetLaddersConfig;

    private final ForgeConfigSpec.ConfigValue<Double> swimSpeedMultiplierConfig;

    public boolean allowClimbing = false;
    public boolean targetLadders = false;

    public double swimSpeedMultiplier = 2.5d;

    public Movement(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        this.allowClimbingConfig = Config.builder
                .comment("If true, mobs will be able to climb (up and down)")
                .define("Allow Climbing", this.allowClimbing);
        this.targetLaddersConfig = Config.builder
                .comment("If true, mobs try to find climbable blocks to reach the target")
                .define("Target Ladders", this.targetLadders);
        swimSpeedMultiplierConfig = Config.builder
                .comment("How faster mobs can swim. Setting to 0 will leave the swim speed as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
                .defineInRange("Swim Speed Multiplier", this.swimSpeedMultiplier, 0d, 4d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.allowClimbing = this.allowClimbingConfig.get();
        this.targetLadders = this.targetLaddersConfig.get();
        this.swimSpeedMultiplier = this.swimSpeedMultiplierConfig.get();
    }

    final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("6d2cb27e-e5e3-41b9-8108-f74131a90cce");

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

        if (this.swimSpeedMultiplier != 0d) {
            MCUtils.applyModifier(mob, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Swim Speed Multiplier", this.swimSpeedMultiplier, AttributeModifier.Operation.MULTIPLY_BASE, false);
        }
    }
}