package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Not Tempted Animals", description = "Makes animals not tempted by food. Use the entity type tag enhancedai:can_ignore_food_temptation to change animals.")
@LoadFeature(module = Modules.Ids.ANIMAL)
public class NotTemptedAnimals extends Feature {
    public static final TagKey<EntityType<?>> CAN_IGNORE_FOOD_TEMPTATION = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_ignore_food_temptation"));
    public static final String NOT_TEMPTED = EnhancedAI.RESOURCE_PREFIX + "not_tempted";

    @Config
    @Label(name = "Not tempted chance", description = "Animals have this percentage chance to not be temped by food.")
    public static Double notTemptedChance = 0.5d;

    public NotTemptedAnimals(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal))
            return;

        CompoundTag persistentData = animal.getPersistentData();

        boolean notTempted = NBTUtils.getBooleanOrPutDefault(persistentData, NOT_TEMPTED, animal.getType().is(CAN_IGNORE_FOOD_TEMPTATION) && animal.getRandom().nextDouble() < notTemptedChance);

        if (notTempted) {
            animal.goalSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof TemptGoal);
        }
    }
}