package insane96mcp.enhancedai.module.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.MOBS, description = "Only entity types in `enhancedai:mobs/random_stroll` tag are affected by this feature.")
public class RandomStroll extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_BY_FEATURE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/random_stroll"));

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
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !event.getEntity().getType().is(AFFECTED_BY_FEATURE)
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob))
            return;

		RANDOM_STROLL_CHANCE_MULTIPLIER.applyIfAbsent(mob, randomStrollChanceMultiplier);
    }
}