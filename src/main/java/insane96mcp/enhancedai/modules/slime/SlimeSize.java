package insane96mcp.enhancedai.modules.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;

@LoadFeature(module = Modules.Ids.SLIME, description = "Only entity types in `enhancedai:slime/spawn_size` tag are affected by this feature.")
public class SlimeSize extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("slime/spawn_size"));
    @Config(min = 0, max = 16, description = "Changes the max size a Slime/Magma cube can spawn as. Vanilla is max 4 with 3 excluded. Set to 0 to disable.")
    public static Integer maxSpawnSize = 5;

    public static boolean shouldOverrideSpawnSize(Slime slime) {
        return isEnabled(SlimeSize.class) && maxSpawnSize > 0 && !Spawning.isUnaffectedByFeatures(slime);
    }
}