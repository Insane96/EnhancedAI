package insane96mcp.enhancedai.module.blaze;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class EAIBlazeAttackGoal extends Goal {
    private final net.minecraft.world.entity.monster.Blaze blaze;
    private int attackStep;
    private int attackTime;
    private int lastSeen;

    public EAIBlazeAttackGoal(net.minecraft.world.entity.monster.Blaze blaze) {
        this.blaze = blaze;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
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
                    this.attackTime = BlazeAttack.CHARGE_TIME.get(this.blaze);
                    this.blaze.setCharged(true);
                }
                else if (this.attackStep <= BlazeAttack.FIREBALLS_SHOT.get(this.blaze) + 1) {
                    this.attackTime = BlazeAttack.TIME_BETWEEN_FIREBALLS.get(this.blaze);
                }
                else {
                    this.attackTime = BlazeAttack.RECHARGE_TIME.get(this.blaze);
                    this.attackStep = 0;
                    this.blaze.setCharged(false);
                }

                if (this.attackStep > 1) {
                    double inaccuracy = BlazeAttack.INACCURACY.get(this.blaze);
                    if (inaccuracy == -1)
                        inaccuracy = Math.sqrt(Math.sqrt(distanceSqrToTarget)) * 0.5D;
                    if (!this.blaze.isSilent())
                        this.blaze.level().levelEvent(null, 1018, this.blaze.blockPosition(), 0);

                    for (int i = 0; i < BlazeAttack.FIREBALLS_PER_SHOT.get(this.blaze); i++) {
                        SmallFireball smallfireball = new SmallFireball(this.blaze.level(), this.blaze.getX(), this.blaze.getEyeY(), this.blaze.getZ(), new Vec3(xDir + this.blaze.getRandom().nextGaussian() * inaccuracy, yDir, zDir + this.blaze.getRandom().nextGaussian() * inaccuracy));
                        smallfireball.setOwner(this.blaze);
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
