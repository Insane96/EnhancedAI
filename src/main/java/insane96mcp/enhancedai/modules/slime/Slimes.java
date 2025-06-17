package insane96mcp.enhancedai.modules.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

@LoadFeature(module = Modules.Ids.SLIME, description = "Use `enhancedai:affect_slime_spawn_size` and `enhancedai:affect_slime_jump_rate` entity type tag to add more slimes affected by this feature.")
public class Slimes extends Feature {

    public static final TagKey<EntityType<?>> AFFECT_SLIME_SPAWN_SIZE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("affect_slime_spawn_size"));
    public static final TagKey<EntityType<?>> AFFECT_SLIME_JUMP_RATE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("affect_slime_jump_rate"));
    @Config(min = 0, max = 16, description = "Changes the max size a Slime/Magma cube can spawn as. Vanilla is max 4 with 3 excluded. Set to 0 to disable.")
    public static Integer maxSpawnSize = 5;

    @Config(min = 0d, max = 5d)
    public static Double jumpDelayMultiplier = 0.5d;

    public Slimes(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static boolean shouldOverrideSpawnSize() {
        return isEnabled(Slimes.class) && maxSpawnSize > 0;
    }

    public static boolean shouldChangeJumpDelay() {
        return isEnabled(Slimes.class) && jumpDelayMultiplier != 1d;
    }
}