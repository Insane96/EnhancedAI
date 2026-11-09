package insane96mcp.enhancedai.module.villager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * Makes a badly damaged golem walk to the closest villager able to repair it and wait there.
 * The villager doesn't need to know: {@link insane96mcp.enhancedai.module.villager.behavior.RepairGolemBehavior} starts by itself when the golem is near.
 */
public class GolemSeekRepairGoal extends Goal {
    private static final int SEARCH_INTERVAL = 40;
    private static final int PATH_RECALCULATION_INTERVAL = 20;
    private static final int WALK_TIMEOUT = 400;
    private static final int WAIT_TIMEOUT = 300;
    //Inside the distance at which villagers repair
    private static final double STOP_DISTANCE_SQR = 2.5d * 2.5d;

    private final Mob golem;
    private Villager smith;
    private long nextSearchTime;
    private long nextPathTime;
    private long giveUpTime;
    private boolean arrived;
    private float lastHealth;

    public GolemSeekRepairGoal(Mob golem) {
        this.golem = golem;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.golem.getHealth() >= this.golem.getMaxHealth()
                || this.golem.getTarget() != null
                || this.golem.getHealth() >= this.golem.getMaxHealth() * VillagerGolemRepair.SEEK_HEALTH_THRESHOLD.getOrDefault(this.golem, VillagerGolemRepair.seekHealthThreshold))
            return false;
        long gameTime = this.golem.level().getGameTime();
        if (gameTime < this.nextSearchTime)
            return false;
        this.nextSearchTime = gameTime + SEARCH_INTERVAL;
        if (!this.seeksRepair()
                || !this.canMoveFreely()
                || !VillagerGolemRepair.needsRepair(this.golem)
                || this.isEnemyNear())
            return false;
        this.smith = this.findSmith();
        return this.smith != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.golem.level().getGameTime() < this.giveUpTime
                && this.seeksRepair()
                && this.canMoveFreely()
                && VillagerGolemRepair.needsRepair(this.golem)
                && this.smith.isAlive()
                && VillagerGolemRepair.canRepair(this.smith)
                && !this.isEnemyNear();
    }

    @Override
    public void start() {
        this.giveUpTime = this.golem.level().getGameTime() + WALK_TIMEOUT;
        this.nextPathTime = 0L;
        this.arrived = false;
        this.lastHealth = this.golem.getHealth();
    }

    @Override
    public void tick() {
        long gameTime = this.golem.level().getGameTime();
        this.golem.getLookControl().setLookAt(this.smith, 30f, 30f);
        float health = this.golem.getHealth();
        if (health > this.lastHealth)
            this.giveUpTime = gameTime + WAIT_TIMEOUT;
        this.lastHealth = health;

        if (this.golem.distanceToSqr(this.smith) <= STOP_DISTANCE_SQR) {
            if (!this.arrived) {
                this.arrived = true;
                this.giveUpTime = gameTime + WAIT_TIMEOUT;
            }
            this.golem.getNavigation().stop();
        }
        else if (gameTime >= this.nextPathTime) {
            this.nextPathTime = gameTime + PATH_RECALCULATION_INTERVAL;
            this.golem.getNavigation().moveTo(this.smith, VillagerGolemRepair.SEEK_SPEED.getOrDefault(this.golem, VillagerGolemRepair.seekSpeed));
        }
    }

    @Override
    public void stop() {
        this.golem.getNavigation().stop();
        //A golem that got repaired doesn't need to wait, it just won't be low on health for a while
        if (VillagerGolemRepair.needsRepair(this.golem) || this.golem.getTarget() != null)
            this.nextSearchTime = this.golem.level().getGameTime() + VillagerGolemRepair.SEEK_RETRY_COOLDOWN.getOrDefault(this.golem, VillagerGolemRepair.seekRetryCooldown);
        this.smith = null;
    }

    private boolean seeksRepair() {
        return VillagerGolemRepair.SEEKS_REPAIR.getOrDefault(this.golem, VillagerGolemRepair.golemsSeekRepair);
    }

    private boolean canMoveFreely() {
        return !this.golem.isLeashed() && !this.golem.isPassenger();
    }

    private Villager findSmith() {
        int radius = VillagerGolemRepair.SEEK_RADIUS.getOrDefault(this.golem, VillagerGolemRepair.seekRadius);
        double radiusSqr = radius * radius;
        return this.golem.level().getEntitiesOfClass(Villager.class, this.golem.getBoundingBox().inflate(radius),
                        villager -> this.golem.distanceToSqr(villager) <= radiusSqr && VillagerGolemRepair.canRepair(villager))
                .stream()
                .min(Comparator.comparingDouble(this.golem::distanceToSqr))
                .orElse(null);
    }

    private boolean isEnemyNear() {
        int radius = VillagerGolemRepair.ENEMY_CHECK_RADIUS.getOrDefault(this.golem, VillagerGolemRepair.enemyCheckRadius);
        double radiusSqr = radius * radius;
        return !this.golem.level().getEntitiesOfClass(LivingEntity.class, this.golem.getBoundingBox().inflate(radius),
                entity -> entity instanceof Enemy && entity.isAlive() && this.golem.distanceToSqr(entity) <= radiusSqr).isEmpty();
    }
}
