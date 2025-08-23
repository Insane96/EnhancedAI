package insane96mcp.enhancedai.modules.illager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

@LoadFeature(module = Modules.Ids.ILLAGER, canBeDisabled = false, description = "Adds a new block tag `enhancedai:breakable_by_ravager` for ravager to break (already includes leaves)")
public class RavagerFeature extends Feature {
    public static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK, EnhancedAI.location("breakable_by_ravager"));
}