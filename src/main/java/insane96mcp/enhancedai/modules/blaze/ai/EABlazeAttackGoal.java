package insane96mcp.enhancedai.modules.blaze.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.SmallFireball;

import java.util.EnumSet;

public class EABlazeAttackGoal extends Goal {
    private final Blaze blaze;
    private int attackStep;
    private int attackTime;
    private int lastSeen;

    private int fireballShot = 3;
    private int timeBetweenFireballs = 6;
    private int rechargeTime = 100;
    private int chargeTime = 60;
    private int fireballsPerShot = 1;
    private int inaccuracy = -1;

    public EABlazeAttackGoal(Blaze blaze) {
        this.blaze = blaze;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public EABlazeAttackGoal setFireballShot(int fireballShot) {
        this.fireballShot = fireballShot;
        return this;
    }

    public EABlazeAttackGoal setTimeBetweenFireballs(int timeBetweenFireballs) {
        this.timeBetweenFireballs = timeBetweenFireballs;
        return this;
    }

    public EABlazeAttackGoal setRechargeTime(int rechargeTime) {
        this.rechargeTime = rechargeTime;
        return this;
    }

    public EABlazeAttackGoal setChargeTime(int chargeTime) {
        this.chargeTime = chargeTime;
        return this;
    }

    public EABlazeAttackGoal setFireballsPerShot(int fireballsPerShot) {
        this.fireballsPerShot = fireballsPerShot;
        return this;
    }

    public EABlazeAttackGoal setInaccuracy(int inaccuracy) {
        this.inaccuracy = inaccuracy;
        return this;
    }

    public boolean canUse() {
        LivingEntity livingentity = this.blaze.getTarget();
        return livingentity != null && livingentity.isAlive() && this.blaze.canAttack(livingentity);
    }

    public void start() {
        this.attackStep = 0;
    }

    public void stop() {
        this.blaze.setCharged(false);
        this.lastSeen = 0;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        --this.attackTime;
        LivingEntity livingentity = this.blaze.getTarget();
        if (livingentity == null)
            return;

        boolean hasLineOfSight = this.blaze.getSensing().hasLineOfSight(livingentity);
        if (hasLineOfSight) {
            this.lastSeen = 0;
        }
        else {
            ++this.lastSeen;
        }

        double distanceSqrToTarget = this.blaze.distanceToSqr(livingentity);
        if (distanceSqrToTarget < 4.0D) {
            if (!hasLineOfSight) {
                return;
            }

            if (this.attackTime <= 0) {
                this.attackTime = 20;
                this.blaze.doHurtTarget(livingentity);
            }

            this.blaze.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0D);
        }
        else if (distanceSqrToTarget < this.getFollowDistance() * this.getFollowDistance() && hasLineOfSight) {
            double xDir = livingentity.getX() - this.blaze.getX();
            double yDir = livingentity.getY(0.3d) - this.blaze.getY(0.5d);
            double zDir = livingentity.getZ() - this.blaze.getZ();
            if (this.attackTime <= 0) {
                ++this.attackStep;
                if (this.attackStep == 1) {
                    this.attackTime = this.chargeTime;
                    this.blaze.setCharged(true);
                }
                else if (this.attackStep <= this.fireballShot + 1) {
                    this.attackTime = this.timeBetweenFireballs;
                }
                else {
                    this.attackTime = this.rechargeTime;
                    this.attackStep = 0;
                    this.blaze.setCharged(false);
                }

                if (this.attackStep > 1) {
                    double inaccuracy;
                    if (this.inaccuracy == -1)
                        inaccuracy = Math.sqrt(Math.sqrt(distanceSqrToTarget)) * 0.5D;
                    else
                        inaccuracy = 0.3d * this.inaccuracy;
                    if (!this.blaze.isSilent()) {
                        this.blaze.level().levelEvent(null, 1018, this.blaze.blockPosition(), 0);
                    }

                    for (int i = 0; i < this.fireballsPerShot; i++) {
                        SmallFireball smallfireball = new SmallFireball(this.blaze.level(), this.blaze, xDir + this.blaze.getRandom().nextGaussian() * inaccuracy, yDir, zDir + this.blaze.getRandom().nextGaussian() * inaccuracy);
                        smallfireball.setPos(smallfireball.getX(), this.blaze.getY(0.5D) + 0.5D, smallfireball.getZ());
                        this.blaze.level().addFreshEntity(smallfireball);
                    }
                }
            }

            this.blaze.getLookControl().setLookAt(livingentity, 10.0F, 10.0F);
        }
        else if (this.lastSeen < 5) {
            this.blaze.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0D);
        }

        super.tick();
    }

    private double getFollowDistance() {
        return this.blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
}
