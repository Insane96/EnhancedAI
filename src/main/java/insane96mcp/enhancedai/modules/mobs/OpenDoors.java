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

@LoadFeature(module = Modules.Ids.MOBS, description = "Only entity types in `enhancedai:mobs/can_open_doors` tag are affected by this feature.")
public class OpenDoors extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_open_doors"));

	public static EAIData<Boolean> CAN_OPEN_DOORS;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CAN_OPEN_DOORS = EAIData.ofBool(this.createDataKey("can_open_doors"), (mob, canOpenDoors) -> {
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
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !shouldBeAbleToOpenDoors(mob)
                || event.getLevel().isClientSide)
            return;

		CAN_OPEN_DOORS.applyIfAbsent(mob, true);
    }

    public static boolean shouldBeAbleToOpenDoors(Mob mob) {
        return Feature.isEnabled(OpenDoors.class) && mob.getType().is(AFFECTED_ENTITY_TYPES);
    }
}