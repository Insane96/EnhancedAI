package insane96mcp.enhancedai.module.villager.behavior;

import insane96mcp.enhancedai.module.villager.VillagerGolemRepair;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public class RepairGolemBehavior extends Behavior<Villager> {
    private static final int MAX_DURATION = 200;
    private static final int SCAN_INTERVAL = 20;
    private static final int HEAD_TURN_TICKS = 20;
    private static final float WALK_SPEED = 0.5f;
    private static final int WALK_CLOSE_ENOUGH_DISTANCE = 2;
    //More than vanilla's interaction distance: MoveToTargetSink stops at a Manhattan distance between block positions, so the villager can stop further than that
    private static final double REPAIR_DISTANCE_SQR = 9d;
    //Further than this the enemy check can't see around the golem anymore
    private static final double MAX_DISTANCE_SQR = 16d * 16d;
    private static final float DEFAULT_DROP_CHANCE = 0.085f;

    private long nextScanTime;
    private long startTime;
    private boolean repaired;
    private LivingEntity golem;
    private ItemStack heldIngot = ItemStack.EMPTY;

    public RepairGolemBehavior() {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ), MAX_DURATION);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        long gameTime = level.getGameTime();
        if (gameTime < this.nextScanTime)
            return false;
        this.nextScanTime = gameTime + SCAN_INTERVAL;
        clearLeftoverIngot(villager);
        if (!VillagerGolemRepair.canRepair(villager))
            return false;
        int radius = VillagerGolemRepair.REPAIR_RADIUS.getOrDefault(villager, VillagerGolemRepair.repairRadius);
        double radiusSqr = radius * radius;
        this.golem = villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .flatMap((NearestVisibleLivingEntities entities) -> entities.findClosest(entity -> VillagerGolemRepair.needsRepair(entity)
                        && villager.distanceToSqr(entity) <= radiusSqr
                        && !isEnemyNear(villager, entity)))
                .orElse(null);
        return this.golem != null;
    }

    @Override
    protected void start(ServerLevel level, Villager villager, long gameTime) {
        this.startTime = gameTime;
        this.repaired = false;
        //The same slot is used to show trades to players
        if (villager.getMainHandItem().isEmpty() && !villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET)) {
            this.heldIngot = new ItemStack(Items.IRON_INGOT);
            villager.setItemSlot(EquipmentSlot.MAINHAND, this.heldIngot);
            villager.setDropChance(EquipmentSlot.MAINHAND, 0f);
        }
    }

    //Must fail as soon as the villager is not calm: a walk target written during panic would stop it from fleeing
    @Override
    protected boolean canStillUse(ServerLevel level, Villager villager, long gameTime) {
        return !this.repaired
                && this.hasRequiredMemories(villager)
                && VillagerGolemRepair.canRepair(villager)
                && VillagerGolemRepair.needsRepair(this.golem)
                && villager.distanceToSqr(this.golem) <= MAX_DISTANCE_SQR
                && !isEnemyNear(villager, this.golem)
                && !this.couldNotReachGolem(villager);
    }

    @Override
    protected void tick(ServerLevel level, Villager villager, long gameTime) {
        if (villager.distanceToSqr(this.golem) > REPAIR_DISTANCE_SQR) {
            BehaviorUtils.setWalkAndLookTargetMemories(villager, this.golem, WALK_SPEED, WALK_CLOSE_ENOUGH_DISTANCE);
            return;
        }
        BehaviorUtils.lookAtEntity(villager, this.golem);
        if (gameTime - this.startTime >= HEAD_TURN_TICKS) {
            this.golem.heal(VillagerGolemRepair.REPAIR_AMOUNT.getOrDefault(villager, VillagerGolemRepair.repairAmount).floatValue());
            this.playRepairEffects(level);
            this.repaired = true;
        }
    }

    @Override
    protected void stop(ServerLevel level, Villager villager, long gameTime) {
        this.nextScanTime = gameTime + VillagerGolemRepair.REPAIR_COOLDOWN.getOrDefault(villager, VillagerGolemRepair.repairCooldown);
        Brain<Villager> brain = villager.getBrain();
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        if (!this.heldIngot.isEmpty() && villager.getMainHandItem() == this.heldIngot)
            clearMainHand(villager);
        this.heldIngot = ItemStack.EMPTY;
        this.golem = null;
    }

    /**
     * A save during a repair leaves the ingot in hand. While the behavior is stopped an ingot can only be that one or a trade being shown to a player.
     */
    private static void clearLeftoverIngot(Villager villager) {
        if (villager.getMainHandItem().is(Items.IRON_INGOT) && !villager.getBrain().hasMemoryValue(MemoryModuleType.INTERACTION_TARGET))
            clearMainHand(villager);
    }

    private static void clearMainHand(Villager villager) {
        villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        villager.setDropChance(EquipmentSlot.MAINHAND, DEFAULT_DROP_CHANCE);
    }

    private static boolean isEnemyNear(Villager villager, LivingEntity golem) {
        int radius = VillagerGolemRepair.ENEMY_CHECK_RADIUS.getOrDefault(villager, VillagerGolemRepair.enemyCheckRadius);
        double radiusSqr = radius * radius;
        List<LivingEntity> entities = villager.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of());
        for (LivingEntity entity : entities) {
            if (entity instanceof Enemy
                    && entity.isAlive()
                    && (entity.distanceToSqr(villager) <= radiusSqr || entity.distanceToSqr(golem) <= radiusSqr))
                return true;
        }
        return false;
    }

    /**
     * The memory may be left over from a previous walk, only one set during this run counts
     */
    private boolean couldNotReachGolem(Villager villager) {
        return villager.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                .filter(since -> since >= this.startTime)
                .isPresent();
    }

    private void playRepairEffects(ServerLevel level) {
        RandomSource random = this.golem.getRandom();
        this.golem.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1f, 1f + (random.nextFloat() - random.nextFloat()) * 0.2f);
        double spreadXZ = this.golem.getBbWidth() * 0.4d;
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.IRON_INGOT)), this.golem.getX(), this.golem.getY(0.5d), this.golem.getZ(), 12, spreadXZ, this.golem.getBbHeight() * 0.3d, spreadXZ, 0.05d);
    }
}
