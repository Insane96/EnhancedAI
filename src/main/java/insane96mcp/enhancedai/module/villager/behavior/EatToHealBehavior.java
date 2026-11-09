package insane96mcp.enhancedai.module.villager.behavior;

import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.module.villager.VillagerEatToHeal;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class EatToHealBehavior extends Behavior<Villager> {
    private static final int EAT_TICKS = 32;
    private static final int EFFECTS_INTERVAL = 4;

    private long nextMealTime;
    private long mealEndTime;
    private int foodSlot;
    private FoodProperties food;
    private ItemStack heldFood = ItemStack.EMPTY;

    public EatToHealBehavior() {
        super(Map.of(
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_HOSTILE, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT
        ), EAT_TICKS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        if (villager.getHealth() >= villager.getMaxHealth()
                || level.getGameTime() < this.nextMealTime
                || !Feature.isEnabled(VillagerEatToHeal.class)
                || !villager.getType().is(VillagerEatToHeal.AFFECTED_ENTITY_TYPES)
                || Spawning.isUnaffectedByFeatures(villager)
                || villager.getHealth() >= villager.getMaxHealth() * VillagerEatToHeal.HEALTH_THRESHOLD.getOrDefault(villager, VillagerEatToHeal.healthThreshold)
                || !isCalm(villager))
            return false;
        //A healing food can be left in hand if the villager was saved while eating
        ItemStack mainHand = villager.getMainHandItem();
        if (!mainHand.isEmpty() && !mainHand.is(VillagerEatToHeal.HEALING_FOOD))
            return false;
        return this.pickFood(villager);
    }

    @Override
    protected void start(ServerLevel level, Villager villager, long gameTime) {
        this.mealEndTime = gameTime + EAT_TICKS;
        this.heldFood = villager.getInventory().getItem(this.foodSlot).copyWithCount(1);
        villager.setItemSlot(EquipmentSlot.MAINHAND, this.heldFood);
        villager.setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager villager, long gameTime) {
        return this.hasRequiredMemories(villager)
                && isCalm(villager)
                && ItemStack.isSameItem(villager.getInventory().getItem(this.foodSlot), this.heldFood);
    }

    @Override
    protected void tick(ServerLevel level, Villager villager, long gameTime) {
        if (gameTime >= this.mealEndTime) {
            villager.eat(level, villager.getInventory().getItem(this.foodSlot), this.food);
            villager.heal(healAmount(villager, this.food));
            this.nextMealTime = gameTime + VillagerEatToHeal.MEAL_COOLDOWN.getOrDefault(villager, VillagerEatToHeal.mealCooldown);
        }
        else if ((this.mealEndTime - gameTime) % EFFECTS_INTERVAL == 0) {
            this.playEatingEffects(level, villager);
        }
    }

    @Override
    protected void stop(ServerLevel level, Villager villager, long gameTime) {
        if (villager.getMainHandItem() == this.heldFood) {
            villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            villager.setDropChance(EquipmentSlot.MAINHAND, 0.085f);
        }
        this.heldFood = ItemStack.EMPTY;
    }

    private static boolean isCalm(Villager villager) {
        Brain<Villager> brain = villager.getBrain();
        return !villager.isSleeping()
                && !villager.isTrading()
                && !brain.isActive(Activity.PANIC)
                && !brain.isActive(Activity.PRE_RAID)
                && !brain.isActive(Activity.RAID)
                && !brain.isActive(Activity.HIDE);
    }

    /**
     * Picks the food in the inventory whose healing is closest to the missing health
     */
    private boolean pickFood(Villager villager) {
        SimpleContainer inventory = villager.getInventory();
        float missingHealth = villager.getMaxHealth() - villager.getHealth();
        float bestDifference = Float.MAX_VALUE;
        boolean found = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(VillagerEatToHeal.HEALING_FOOD) || !canSpare(villager, stack))
                continue;
            FoodProperties properties = stack.getFoodProperties(villager);
            if (properties == null)
                continue;
            float difference = Math.abs(healAmount(villager, properties) - missingHealth);
            if (difference < bestDifference) {
                bestDifference = difference;
                this.foodSlot = slot;
                this.food = properties;
                found = true;
            }
        }
        return found;
    }

    private static boolean canSpare(Villager villager, ItemStack stack) {
        int cost = Villager.FOOD_POINTS.getOrDefault(stack.getItem(), 0);
        if (cost == 0)
            return true;
        SimpleContainer inventory = villager.getInventory();
        int foodPoints = Villager.FOOD_POINTS.entrySet().stream()
                .mapToInt(entry -> inventory.countItem(entry.getKey()) * entry.getValue())
                .sum();
        return foodPoints - cost >= VillagerEatToHeal.MIN_FOOD_POINTS_TO_KEEP.getOrDefault(villager, VillagerEatToHeal.minFoodPointsToKeep);
    }

    private static float healAmount(Villager villager, FoodProperties properties) {
        return (float) (properties.nutrition() * VillagerEatToHeal.HEAL_MULTIPLIER.getOrDefault(villager, VillagerEatToHeal.healMultiplier));
    }

    private void playEatingEffects(ServerLevel level, Villager villager) {
        RandomSource random = villager.getRandom();
        villager.playSound(villager.getEatingSound(this.heldFood), 0.5f + 0.5f * random.nextInt(2), (random.nextFloat() - random.nextFloat()) * 0.2f + 1f);
        Vec3 mouth = villager.getEyePosition().add(villager.getLookAngle().scale(0.3d)).subtract(0d, 0.2d, 0d);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, this.heldFood), mouth.x, mouth.y, mouth.z, 5, 0.05d, 0.05d, 0.05d, 0.05d);
    }
}
