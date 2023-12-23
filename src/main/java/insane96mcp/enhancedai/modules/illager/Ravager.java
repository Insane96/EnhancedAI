package insane96mcp.enhancedai.modules.illager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

@Label(name = "Ravager", description = "Changes to Ravagers")
@LoadFeature(module = Modules.Ids.ILLAGER, canBeDisabled = false)
public class Ravager extends Feature {
    public static final TagKey<Block> BREAKABLE_BY_RAVAGER = TagKey.create(Registries.BLOCK, new ResourceLocation(EnhancedAI.MOD_ID, "breakable_by_ravager"));

    public Ravager(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }
}