package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = Modules.Ids.MOBS, description = "Use `enhancedai:can_open_doors` to add more mobs that can open doors.")
public class OpenDoors extends Feature {
    public static final TagKey<EntityType<?>> CAN_OPEN_DOORS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_open_doors"));

    public OpenDoors(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !shouldBeAbleToOpenDoors(mob)
                || event.getLevel().isClientSide)
            return;

        if (mob.getNavigation() instanceof GroundPathNavigation groundPathNavigation) {
            groundPathNavigation.setCanOpenDoors(true);
            groundPathNavigation.setCanPassDoors(true);
            List<Goal> toRemove = new ArrayList<>();
            for (var wrappedGoal : mob.goalSelector.getAvailableGoals()) {
                if (wrappedGoal.getGoal() instanceof OpenDoorGoal) {
                    toRemove.add(wrappedGoal.getGoal());
                }
            }
            toRemove.forEach(mob.goalSelector::removeGoal);
            mob.goalSelector.addGoal(2, new OpenDoorGoal(mob, false));
        }
    }

    public static boolean shouldBeAbleToOpenDoors(Mob mob) {
        return Feature.isEnabled(OpenDoors.class) && mob.getType().is(CAN_OPEN_DOORS);
    }
}