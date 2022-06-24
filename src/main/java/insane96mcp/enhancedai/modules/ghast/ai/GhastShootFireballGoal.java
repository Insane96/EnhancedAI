package insane96mcp.enhancedai.modules.ghast.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GhastShootFireballGoal extends Goal {
    private final Ghast ghast;
    public int chargeTime;

    private int attackCooldown;
    private int fireballsToShot;
    private boolean ignoreLineOfSight;

    private int cooldownBetweenFireballs = 4;
    private int fireballsShot = 0;

    public GhastShootFireballGoal(Ghast p_32776_) {
        this.ghast = p_32776_;
    }

    public boolean canUse() {
        return this.ghast.getTarget() != null;
    }

    public void start() {
        this.chargeTime = -10;
    }

    public void stop() {
        this.ghast.setCharging(false);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = this.ghast.getTarget();
        if (target == null)
            return;

        // 64d
        if (target.distanceToSqr(this.ghast) < 4096d && (this.ghast.hasLineOfSight(target) || this.ignoreLineOfSight)) {
            Level level = this.ghast.level;
            ++this.chargeTime;
            if (this.chargeTime == 0 && !this.ghast.isSilent()) {
                level.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
            }

            if (this.chargeTime >= 10) {
                //TODO try to stop moving when shooting
                if (--this.cooldownBetweenFireballs == 0) {
                    Vec3 vec3 = this.ghast.getViewVector(1f);
                    double dirX = target.getX() - (this.ghast.getX() + vec3.x * 4d);
                    double dirY = target.getY(0.7d) - (this.ghast.getY(0.5d));
                    double dirZ = target.getZ() - (this.ghast.getZ() + vec3.z * 4d);
                    if (!this.ghast.isSilent()) {
                        level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                    }

                    double randomVariation = 0.5d * (this.fireballsToShot - 1);
                    LargeFireball largefireball = new LargeFireball(level, this.ghast, dirX + Mth.nextDouble(ghast.getRandom(), -randomVariation, randomVariation), dirY, dirZ + Mth.nextDouble(ghast.getRandom(), -randomVariation, randomVariation), this.ghast.getExplosionPower());
                    largefireball.setPos(this.ghast.getX() + vec3.x * 3d, this.ghast.getY(0.5d) - 0.3d, largefireball.getZ() + vec3.z * 3d);
                    level.addFreshEntity(largefireball);
                    this.fireballsShot++;
                    this.cooldownBetweenFireballs = 4;
                }
                if (this.fireballsShot == this.fireballsToShot) {
                    this.chargeTime = -this.attackCooldown;
                    if (this.ignoreLineOfSight)
                        this.chargeTime /= 4;
                    this.fireballsShot = 0;
                }
            }
        } else if (this.chargeTime > 0) {
            --this.chargeTime;
        }

        this.ghast.setCharging(this.chargeTime > 0);
    }

    public GhastShootFireballGoal setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
        return this;
    }

    public GhastShootFireballGoal setFireballsToShot(int fireballsToShot) {
        this.fireballsToShot = fireballsToShot;
        return this;
    }

    public GhastShootFireballGoal setIgnoreLineOfSight(boolean ignoreLineOfSight) {
        this.ignoreLineOfSight = ignoreLineOfSight;
        return this;
    }
}
