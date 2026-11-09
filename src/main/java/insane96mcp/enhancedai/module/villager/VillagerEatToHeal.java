package insane96mcp.enhancedai.module.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.VILLAGER, description = "Wounded villagers eat food from their inventory to heal when they are not in danger. Use the enhancedai:villager/eats_to_heal entity type tag to add more villagers, only works with entities that extend vanilla Villagers. Use the enhancedai:villager/healing_food item tag to change the food they can eat.")
public class VillagerEatToHeal extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager/eats_to_heal"));
    public static final TagKey<Item> HEALING_FOOD = TagKey.create(Registries.ITEM, EnhancedAI.location("villager/healing_food"));

    @Config(min = 0d, max = 1d, description = "Villagers eat only when their health is below this percentage of max health")
    public static Double healthThreshold = 0.75d;
    @Config(min = 0d, description = "Health healed by a meal is the food's nutrition multiplied by this value")
    public static Double healMultiplier = 1d;
    @Config(min = 0, description = "Ticks between two meals of the same villager")
    public static Integer mealCooldown = 600;
    @Config(min = 0, description = "Villagers won't eat if that would bring the food points in their inventory below this value. Food points are what villagers use to breed (bread is worth 4, carrots, potatoes and beetroots 1).")
    public static Integer minFoodPointsToKeep = 0;

    public static EAIData<Double> HEALTH_THRESHOLD;
    public static EAIData<Double> HEAL_MULTIPLIER;
    public static EAIData<Integer> MEAL_COOLDOWN;
    public static EAIData<Integer> MIN_FOOD_POINTS_TO_KEEP;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        HEALTH_THRESHOLD = EAIData.ofDouble(this.createDataKey("health_threshold"));
        HEAL_MULTIPLIER = EAIData.ofDouble(this.createDataKey("heal_multiplier"));
        MEAL_COOLDOWN = EAIData.ofInt(this.createDataKey("meal_cooldown"));
        MIN_FOOD_POINTS_TO_KEEP = EAIData.ofInt(this.createDataKey("min_food_points_to_keep"));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Villager villager)
                || !villager.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        HEALTH_THRESHOLD.applyIfAbsent(villager, healthThreshold);
        HEAL_MULTIPLIER.applyIfAbsent(villager, healMultiplier);
        MEAL_COOLDOWN.applyIfAbsent(villager, mealCooldown);
        MIN_FOOD_POINTS_TO_KEEP.applyIfAbsent(villager, minFoodPointsToKeep);
    }
}
