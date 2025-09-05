package insane96mcp.enhancedai.modules.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

@LoadFeature(module = Modules.Ids.SLIME, description = "Make magma cubes swim in lava faster.Only entity types in `enhancedai:slime/magma_cube_surf_speed` tag are affected by this feature.")
public class MagmaCubeSurfSpeed extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("slime/magma_cube_surf_speed"));
    @Config(min = 0)
    public static Double speedMultiplier = 3d;

    public static double getSpeedMultiplier() {
        return isEnabled(MagmaCubeSurfSpeed.class) ? speedMultiplier : 1d;
    }
}