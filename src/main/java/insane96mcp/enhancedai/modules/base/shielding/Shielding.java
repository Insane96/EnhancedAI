package insane96mcp.enhancedai.modules.base.shielding;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Shielding", description = "Makes mobs be able to use shields.")
@LoadFeature(module = Modules.Ids.BASE, enabledByDefault = false)
public class Shielding extends Feature {

    public Shielding(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob))
            return;

        mob.goalSelector.addGoal(3, new ShieldingGoal(mob, 1d));
    }
}