package insane96mcp.enhancedai.modules.ghast.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
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
        this.chargeTime = 0;
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

        double d0 = 64.0D;
        if (target.distanceToSqr(this.ghast) < 4096.0D && this.ghast.hasLineOfSight(target)) {
            Level level = this.ghast.level;
            ++this.chargeTime;
            if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                level.levelEvent((Player)null, 1015, this.ghast.blockPosition(), 0);
            }

            if (this.chargeTime == 20) {
                double d1 = 4.0D;
                Vec3 vec3 = this.ghast.getViewVector(1.0F);
                double d2 = target.getX() - (this.ghast.getX() + vec3.x * 4.0D);
                double d3 = target.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
                double d4 = target.getZ() - (this.ghast.getZ() + vec3.z * 4.0D);
                if (!this.ghast.isSilent()) {
                    level.levelEvent((Player)null, 1016, this.ghast.blockPosition(), 0);
                }

                for (int i = 0; i < this.fireballShot; i++) {
                    LargeFireball largefireball = new LargeFireball(level, this.ghast, d2, d3, d4, this.ghast.getExplosionPower());
                    largefireball.setPos(this.ghast.getX() + vec3.x * 4.0D, this.ghast.getY(0.5D) + 0.5D, largefireball.getZ() + vec3.z * 4.0D);
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
