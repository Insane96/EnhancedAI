package insane96mcp.enhancedai.modules.pillager;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Misc")
@LoadFeature(module = Modules.Ids.ILLAGER)
public class MiscIllagers extends Feature {

    @Config
    @Label(name = "Can Open Doors", description = "Makes every illager able to open doors even outside raids. (In vanilla only vindicators can open doors during raids)")
    public static Boolean canOpenDoors = true;

    public MiscIllagers(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onHit(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof AbstractIllager illager)
                || event.getLevel().isClientSide)
            return;

        if (canOpenDoors && illager.getNavigation() instanceof GroundPathNavigation groundPathNavigation) {
            groundPathNavigation.setCanOpenDoors(true);
            List<Goal> toRemove = new ArrayList<>();
            for (var wrappedGoal : illager.goalSelector.getAvailableGoals()) {
                if (wrappedGoal.getGoal() instanceof OpenDoorGoal) {
                    toRemove.add(wrappedGoal.getGoal());
                }
            }
            toRemove.forEach(illager.goalSelector::removeGoal);
            illager.goalSelector.addGoal(2, new OpenDoorGoal(illager, false));
        }
    }
}