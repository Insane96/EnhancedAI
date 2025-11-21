package insane96mcp.enhancedai.modules.mobs.sprint;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
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

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes mobs be able to move in more ways, like climbing or swim. Only entity types in `enhancedai:mobs/can_sprint` tag are affected by this feature.")
public class Sprint extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_sprint"));

	@Config(min = 0, max = 1d)
	public static Double chance = 0.25d;

	public static EAIData<Boolean> CAN_SPRINT_DATA;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CAN_SPRINT_DATA = EAIData.ofBool(this.createDataKey("can_sprint"), (mob, canSprint) -> {
			GoalHelper.removeGoal(mob.goalSelector, SprintGoal.class);
			if (canSprint)
				mob.goalSelector.addGoal(1, new SprintGoal(mob));
		});
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		CAN_SPRINT_DATA.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
    }
}