package insane96mcp.enhancedai.module.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;

@LoadFeature(module = EAIModules.SLIME, description = "Make magma cubes swim in lava faster. Only entity types in `enhancedai:slime/magma_cube_surf_speed` tag are affected by this feature.")
public class MagmaCubeSurfSpeed extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("slime/magma_cube_surf_speed"));
    @Config(min = 0)
    public static Double speedMultiplier = 3d;

    public static double getSpeedMultiplier(MagmaCube magmaCube) {
        return isEnabled(MagmaCubeSurfSpeed.class) && !Spawning.isUnaffectedByFeatures(magmaCube) ? speedMultiplier : 1d;
    }
}