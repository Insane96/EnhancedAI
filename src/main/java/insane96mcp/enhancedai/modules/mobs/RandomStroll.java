package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Only entity types in `enhancedai:mobs/random_stroll` tag are affected by this feature.")
public class RandomStroll extends Feature {
	@Config(min = 0d, description = "Multiplies the chance for a mob to randomly stroll by this value. (lower values = higher chance)")
	public static Double randomStrollChanceMultiplier = 0.65d;

	@Config(description = "In vanilla mobs will stop moving if farther than 32/48 blocks from the player. This removes the check.")
	public static Boolean allowRandomStrollAwayFromPlayer = true;

	public static EAIData<Double> RANDOM_STROLL_CHANCE_MULTIPLIER;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		RANDOM_STROLL_CHANCE_MULTIPLIER = EAIData.ofDouble(this.createDataKey("random_stroll_chance_multiplier"));
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob))
            return;

		RANDOM_STROLL_CHANCE_MULTIPLIER.applyIfAbsent(mob, randomStrollChanceMultiplier);
    }
}