package insane96mcp.enhancedai.modules.shulker;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

@Label(name = "Shulker Bullets")
@LoadFeature(module = Modules.Ids.SHULKER)
public class ShulkerBullets extends Feature {
    @Config(min = 1, max = 600)
    @Label(name = "Leviation Duration")
    public static Difficulty levitationDuration = new Difficulty(200, 100, 50);
    @Config(min = 0, max = 127)
    @Label(name = "Leviation Amplifier", description = "Note that 0 = I, 1 = II, and so on")
    public static Difficulty levitationAmplifier = new Difficulty(1, 3, 7);

    public ShulkerBullets(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static MobEffectInstance getLevitationInstance(Level level, MobEffectInstance originalValue) {
        if (!isEnabled(ShulkerBullets.class))
            return originalValue;

        return new MobEffectInstance(MobEffects.LEVITATION, (int) levitationDuration.getByDifficulty(level), (int) levitationAmplifier.getByDifficulty(level));
    }
}