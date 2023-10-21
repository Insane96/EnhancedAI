package insane96mcp.enhancedai.modules.slime;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

@Label(name = "Slimes")
@LoadFeature(module = Modules.Ids.SLIME)
public class Slimes extends Feature {

    public static final TagKey<EntityType<?>> AFFECT_SLIME_SPAWN_SIZE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "affect_slime_spawn_size"));
    public static final TagKey<EntityType<?>> AFFECT_SLIME_JUMP_RATE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "affect_slime_jump_rate"));
    @Config(min = 0, max = 16)
    @Label(name = "Max spawn size", description = "Changes the max size a Slime/Magma cube can spawn as. Vanilla is max 4 with 3 excluded. Set to 0 to disable. Only slimes in enhancedai:affect_slime_spawn_size entity type tag are affected by this.")
    public static Integer maxSpawnSize = 6;

    /*@Config
    @Label(name = "Faster swimming")
    public static Boolean fasterSwimming = true;*/

    @Config(min = 0d, max = 5d)
    @Label(name = "Jump delay multiplier", description = "Only slimes in enhancedai:affect_slime_jump_rate entity type tag are affected by this.")
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