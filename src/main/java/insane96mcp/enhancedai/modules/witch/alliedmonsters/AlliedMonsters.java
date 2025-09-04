package insane96mcp.enhancedai.modules.witch.alliedmonsters;

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
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WITCH, description = "Witches can throw potions to monsters. Only witches in the `witch/allied_monsters/can_target_monsters` entity type tag will be able to target monsters. The monsters targeted will be in the `witch/allied_monsters/eligible_targets` entity type tag.")
public class AlliedMonsters extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch/allied_monsters/can_target_monsters"));
	public static final TagKey<EntityType<?>> ELIGIBLE_TARGETS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch/allied_monsters/eligible_targets"));

    public static EAIData<Boolean> ALLIED_MONSTERS;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        ALLIED_MONSTERS = EAIData.ofBool(this.createDataKey("allied_monsters"), (witch, alliedMonsters) -> {
            GoalHelper.removeGoal(witch.targetSelector, WitchTargetMonsterGoal.class);
            if (alliedMonsters)
                witch.targetSelector.addGoal(2, new WitchTargetMonsterGoal<>(witch, Mob.class, false, false, TargetingConditions.forNonCombat().selector(livingEntity -> livingEntity.getType().is(ELIGIBLE_TARGETS))));
        });
    }

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Witch witch)
				|| !witch.getType().is(AFFECTED_ENTITY_TYPES))
			return;

        ALLIED_MONSTERS.applyIfAbsent(witch, true);
	}
}
