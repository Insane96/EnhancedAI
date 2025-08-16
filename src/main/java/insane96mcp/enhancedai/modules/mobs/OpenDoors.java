package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Use `enhancedai:open_doors/can_open_doors` to add more mobs that can open doors.")
public class OpenDoors extends Feature {
    public static final TagKey<EntityType<?>> CAN_OPEN_DOORS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("open_doors/can_open_doors"));

	public static EAIData<Boolean> CAN_OPEN_DOORS_DATA;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CAN_OPEN_DOORS_DATA = EAIData.ofBool(this.createDataKey("can_open_doors"), (mob, canOpenDoors) -> {
			GoalHelper.removeGoal(mob.goalSelector, OpenDoorGoal.class);
			if (canOpenDoors)
				mob.goalSelector.addGoal(2, new OpenDoorGoal(mob, false));
			if (mob.getNavigation() instanceof GroundPathNavigation groundPathNavigation) {
				groundPathNavigation.setCanOpenDoors(canOpenDoors);
			}
		});
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !shouldBeAbleToOpenDoors(mob)
                || event.getLevel().isClientSide)
            return;

		CAN_OPEN_DOORS_DATA.applyIfAbsent(mob, true);
    }

    public static boolean shouldBeAbleToOpenDoors(Mob mob) {
        return Feature.isEnabled(OpenDoors.class) && mob.getType().is(CAN_OPEN_DOORS);
    }
}