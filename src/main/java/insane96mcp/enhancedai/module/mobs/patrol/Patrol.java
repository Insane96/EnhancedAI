package insane96mcp.enhancedai.module.mobs.patrol;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.data.EAIDataBlockPos;
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

@LoadFeature(module = EAIModules.MOBS, description = "Mobs with this feature enabled will move back to the position they spawned in if getting too far away from it.")
public class Patrol extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_patrol"));

    @Config(min = 0)
    public static Double range = 32d;

    public static EAIData<Boolean> PATROL;
    public static EAIDataBlockPos POS;
    public static EAIData<Double> RANGE;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        PATROL = EAIData.ofBool(this.createDataKey("patrol"), (mob, patrol) -> {
            GoalHelper.removeGoal(mob.goalSelector, PatrolGoal.class);
            if (patrol)
                mob.goalSelector.addGoal(1, new PatrolGoal(mob));
        });
        POS = EAIDataBlockPos.of(this.createDataKey("pos"));
        RANGE = EAIData.ofDouble(this.createDataKey("range"));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        PATROL.applyIfAbsent(mob, true);
        POS.applyIfAbsent(mob, mob.blockPosition());
        RANGE.applyIfAbsent(mob, range);
    }
}