package insane96mcp.enhancedai.modules.mobs.movement;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Movement", description = "Makes mobs be able to move in more ways, like climbing or swim.")
@LoadFeature(module = Modules.Ids.MOBS)
public class Movement extends Feature {

    final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("6d2cb27e-e5e3-41b9-8108-f74131a90cce");

    @Config
    @Label(name = "Allow Climbing", description = "If true, mobs will be able to climb (up and down)")
    public static Boolean allowClimbing = true;
    @Config
    @Label(name = "Target Ladders", description = "If true, mobs try to find climbable blocks to reach the target")
    public static Boolean targetLadders = false;

    @Config(min = 0d, max = 4d)
    @Label(name = "Swim Speed Multiplier", description = "How faster mobs can swim. Setting to 0 will leave the swim speed as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
    public static Double swimSpeedMultiplier = 2.5d;

    public Movement(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob))
            return;

        if (allowClimbing) {
            mob.goalSelector.addGoal(3, new ClimbLaddersGoal(mob));
        }

        if (targetLadders) {
            mob.targetSelector.addGoal(0, new FindLaddersGoal(mob));
        }

        if (swimSpeedMultiplier != 0d) {
            MCUtils.applyModifier(mob, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Swim Speed Multiplier", swimSpeedMultiplier, AttributeModifier.Operation.MULTIPLY_BASE, false);
        }
    }
}