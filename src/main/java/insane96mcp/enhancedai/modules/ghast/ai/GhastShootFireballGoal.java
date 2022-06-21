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
    private int fireballShot;

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
        if (target.distanceToSqr(this.ghast) < 4096d && this.ghast.hasLineOfSight(target)) {
            Level level = this.ghast.level;
            ++this.chargeTime;
            if (this.chargeTime == 0 && !this.ghast.isSilent()) {
                level.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
            }

            if (this.chargeTime == 10) {
                Vec3 vec3 = this.ghast.getViewVector(1f);
                double dirX = target.getX() - (this.ghast.getX() + vec3.x * 4d);
                double dirY = target.getY(0.5d) - (0.5d + this.ghast.getY(0.5d));
                double dirZ = target.getZ() - (this.ghast.getZ() + vec3.z * 4d);
                if (!this.ghast.isSilent()) {
                    level.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                }

                double randomVariation = 0.1d * (this.fireballShot - 1);
                for (int i = 0; i < this.fireballShot; i++) {
                    LargeFireball largefireball = new LargeFireball(level, this.ghast, dirX + Mth.nextDouble(ghast.getRandom(), -randomVariation, randomVariation), dirY, dirZ + Mth.nextDouble(ghast.getRandom(), -randomVariation, randomVariation), this.ghast.getExplosionPower());
                    largefireball.setPos(this.ghast.getX() + vec3.x * 4d, this.ghast.getY(0.5d) + 0.5d, largefireball.getZ() + vec3.z * 4d);
                    level.addFreshEntity(largefireball);
                }
                this.chargeTime = -this.attackCooldown;
            }
        } else if (this.chargeTime > 0) {
            --this.chargeTime;
        }

        this.ghast.setCharging(this.chargeTime > 10);
    }

    public GhastShootFireballGoal setAttackCooldown(int attackCooldown) {
        this.attackCooldown = attackCooldown;
        return this;
    }

    public GhastShootFireballGoal setFireballShot(int fireballShot) {
        this.fireballShot = fireballShot;
        return this;
    }
}
