package insane96mcp.enhancedai.module.mobs.climbing;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.MOBS, description = "Makes mobs be able to climb ladders and similar blocks. Only entity types in `enhancedai:mobs/can_climb` tag are affected by this feature.")
public class Climbing extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_climb"));

    @Config(min = 0d, max = 1d)
    public static Double ladderClimbChance = 1d;
    /*@Config(min = 0d, max = 1d)
    public static Double wallClimbChance = 1d;*/

	public static EAIData<Boolean> CAN_CLIMB_LADDERS_DATA;
	public static EAIData<Boolean> CAN_CLIMB_WALLS_DATA;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CAN_CLIMB_LADDERS_DATA = EAIData.ofBool(this.createDataKey("can_climb_ladders"), (mob, canClimb) -> {
			GoalHelper.removeGoal(mob.goalSelector, ClimbClimbableGoal.class);
			if (canClimb)
				mob.goalSelector.addGoal(3, new ClimbClimbableGoal(mob));
		});
        CAN_CLIMB_WALLS_DATA = EAIData.ofBool(this.createDataKey("can_climb_walls"));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		CAN_CLIMB_LADDERS_DATA.applyIfAbsent(mob, mob.getRandom().nextDouble() < ladderClimbChance);
        //CAN_CLIMB_WALLS_DATA.applyIfAbsent(mob, mob.getRandom().nextDouble() < wallClimbChance);
    }
}