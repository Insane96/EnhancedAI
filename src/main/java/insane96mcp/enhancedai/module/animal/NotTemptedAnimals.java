package insane96mcp.enhancedai.module.animal;

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
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.ANIMAL, description = "Makes animals not tempted by food. Use the entity type tag enhancedai:animal/can_ignore_food_temptation to change animals. Please note that re-adding the tempt goal to the mob can't be done at runtime by updating the data with the /enhancedai command.")
public class NotTemptedAnimals extends Feature {
    //TODO Some animals should attack the player with the food in the hand
    public static final TagKey<EntityType<?>> CAN_IGNORE_FOOD_TEMPTATION = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal/can_ignore_food_temptation"));
    public static EAIData<Boolean> NOT_TEMPTED;

    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to not be temped by food.")
    public static Double notTemptedChance = 0.5d;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        NOT_TEMPTED = EAIData.ofBool(this.createDataKey("not_tempted"), (mob, notTempted) -> {
            if (notTempted)
                GoalHelper.removeGoal(mob.goalSelector, TemptGoal.class);
        });
    }

    //Low priority so other mods can set persistent data
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal))
            return;

        NOT_TEMPTED.applyIfAbsent(animal, animal.getType().is(CAN_IGNORE_FOOD_TEMPTATION) && animal.getRandom().nextDouble() < notTemptedChance);
    }
}