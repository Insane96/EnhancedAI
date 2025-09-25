package insane96mcp.enhancedai.modules.mobs.climbing;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes mobs be able to climb ladders, and similar blocks. Only entity types in `enhancedai:mobs/can_climb` tag are affected by this feature.")
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
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		CAN_CLIMB_LADDERS_DATA.applyIfAbsent(mob, mob.getRandom().nextDouble() < ladderClimbChance);
        //CAN_CLIMB_WALLS_DATA.applyIfAbsent(mob, mob.getRandom().nextDouble() < wallClimbChance);
    }
}