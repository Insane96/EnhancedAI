package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ANIMAL, description = "Makes animals not tempted by food. Use the entity type tag enhancedai:animal/can_ignore_food_temptation to change animals.")
public class NotTemptedAnimals extends Feature {
    //TODO Some animals should attack the player with the food in the hand
    public static final TagKey<EntityType<?>> CAN_IGNORE_FOOD_TEMPTATION = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal/can_ignore_food_temptation"));
    public static ResourceLocation NOT_TEMPTED;

    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to not be temped by food.")
    public static Double notTemptedChance = 0.5d;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        NOT_TEMPTED = this.createDataKey("not_tempted");
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal))
            return;

        boolean notTempted = NBTUtils.getBooleanOrPutDefault(animal, NOT_TEMPTED, animal.getType().is(CAN_IGNORE_FOOD_TEMPTATION) && animal.getRandom().nextDouble() < notTemptedChance);

        if (notTempted)
            GoalHelper.removeGoal(animal.goalSelector, TemptGoal.class);
    }
}