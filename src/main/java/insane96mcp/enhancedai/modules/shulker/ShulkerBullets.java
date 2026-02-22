package insane96mcp.enhancedai.modules.shulker;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import javax.annotation.Nullable;

@LoadFeature(module = EAIModules.Ids.SHULKER, description = "Shulker bullets' levitation duration and amplifier based off owner's data")
public class ShulkerBullets extends Feature {
    @Config(min = 1, max = 600)
    public static DifficultyBasedConfig levitationDuration = new DifficultyBasedConfig(100, 100, 160);
    @Config(min = 0, max = 127, description = "Note that 0 = I, 1 = II, and so on")
    public static DifficultyBasedConfig levitationAmplifier = new DifficultyBasedConfig(5, 5, 5);

    public static EAIData<Integer> LEVITATION_DURATION;
    public static EAIData<Integer> LEVITATION_AMPLIFIER;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        LEVITATION_DURATION = EAIData.ofInt(this.createDataKey("levitation_duration"));
        LEVITATION_AMPLIFIER = EAIData.ofInt(this.createDataKey("levitation_amplifier"));
    }

    public static MobEffectInstance getLevitationInstance(Level level, MobEffectInstance originalValue, @Nullable Entity owner) {
        if (!isEnabled(ShulkerBullets.class))
            return originalValue;

        if (!(owner instanceof Mob mob))
            return new MobEffectInstance(MobEffects.LEVITATION, (int) levitationDuration.getByDifficulty(level), (int) levitationAmplifier.getByDifficulty(level));
        else return new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION.get(mob), LEVITATION_AMPLIFIER.get(mob));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Shulker shulker))
            return;

        LEVITATION_DURATION.applyIfAbsent(shulker, (int) levitationDuration.getByDifficulty(event.getLevel()));
        LEVITATION_AMPLIFIER.applyIfAbsent(shulker, (int) levitationAmplifier.getByDifficulty(event.getLevel()));
    }
}