package insane96mcp.enhancedai.module.mobs.parkour;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.MOBS, enabledByDefault = false, description = "Makes mobs be able to leap over a few blocks. Only entity types in the `enhancedai:mobs/can_parkour` tag can leap.")
public class Parkour extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_parkour"));

	public static EAIData<Boolean> CAN_PARKOUR;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CAN_PARKOUR = EAIData.ofBool(this.createDataKey("can_parkour"), (mob, canParkour) -> {
			GoalHelper.removeGoal(mob.goalSelector, ParkourGoal.class);
			if (canParkour)
				mob.goalSelector.addGoal(2, new ParkourGoal(mob));
		});
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		CAN_PARKOUR.applyIfAbsent(mob, true);
    }
}