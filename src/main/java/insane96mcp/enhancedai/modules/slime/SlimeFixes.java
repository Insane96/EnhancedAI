package insane96mcp.enhancedai.modules.slime;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SLIME, description = "Fix slimes and magma cubes dealing damage each tick.")
public class SlimeFixes extends Feature {
    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Slime slime))
            return;

        //MCUtils.applyModifier(slime, ForgeMod.ENTITY_REACH.get(), UUID.fromString("e35ccc07-542a-447b-9387-f4922f6c41c3"), "Enhanced AI Slime fixes", 1d, AttributeModifier.Operation.MULTIPLY_BASE, false);
    }
}