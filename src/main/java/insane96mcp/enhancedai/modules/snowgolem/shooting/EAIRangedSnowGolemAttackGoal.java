package insane96mcp.enhancedai.modules.snowgolem.shooting;

import insane96mcp.enhancedai.ai.EAIRangedAttackGoal;
import insane96mcp.enhancedai.data.EAIData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.projectile.Snowball;

public class EAIRangedSnowGolemAttackGoal extends EAIRangedAttackGoal<SnowGolem> {

    public EAIRangedSnowGolemAttackGoal(SnowGolem mob, double movementSpeedMult, EAIData<Integer> attackCooldown, EAIData<Double> inaccuracy, EAIData<Integer> attackDistance, EAIData<Boolean> canStrafe) {
        super(mob, movementSpeedMult, attackCooldown, inaccuracy, attackDistance, canStrafe);
    }

    @Override
    protected void attackTick(LivingEntity target, double distanceFromTarget, boolean canSeeTarget) {
        double maxAttackDistanceSqr = this.maxAttackDistance.get(this.mob);
        maxAttackDistanceSqr *= maxAttackDistanceSqr;
        if (distanceFromTarget < maxAttackDistanceSqr)
            this.mob.getNavigation().stop();
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--this.attackTime <= 0 && canSeeTarget) {
            this.mob.stopUsingItem();
            attackEntityWithRangedAttack(this.mob, target, 1);
            this.attackTime = this.attackCooldown.get(this.mob);
        }
        if (this.strafingTime > -1 && this.canStrafe()) {
            if (distanceFromTarget > maxAttackDistanceSqr * 0.9F) {
                this.strafingBackwards = false;
            }
            else if (distanceFromTarget < maxAttackDistanceSqr * 0.8F) {
                this.strafingBackwards = true;
            }

            this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
        }
    }

    @Override
    protected void attackEntityWithRangedAttack(SnowGolem entity, LivingEntity target, int chargeTicks) {
        double distance = entity.distanceTo(target);
        double distanceY = target.getY() - entity.getY();
        float f = 1; //distanceFactor / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        Snowball snowball = new Snowball(this.mob.level(), this.mob);
        double dirX = target.getX() - entity.getX();
        double dirZ = target.getZ() - entity.getZ();
        double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double yPos = target.getY(0d);
        yPos += target.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
        double dirY = yPos - snowball.getY();
        snowball.shoot(dirX, dirY + distanceXZ * 0.1d, dirZ, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), this.inaccuracy.get(this.mob).floatValue());
        this.mob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
        entity.level().addFreshEntity(snowball);
    }
}
