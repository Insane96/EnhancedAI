package insane96mcp.enhancedai.modules.shulker.shulkerattack;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.EnumSet;

public class EAShulkerAttackGoal extends Goal {
    private int attackTime;
    private final Shulker shulker;
    private final int baseAttackSpeed;
    private final int bonusHalfSeconds;

    public EAShulkerAttackGoal(Shulker shulker, int baseAttackSpeed, int bonusHalfSeconds) {
        this.shulker = shulker;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.baseAttackSpeed = baseAttackSpeed;
        this.bonusHalfSeconds = bonusHalfSeconds;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        LivingEntity target = this.shulker.getTarget();
        if (target != null && target.isAlive()) {
            return this.shulker.level().getDifficulty() != Difficulty.PEACEFUL && (this.shulker.getHealth() / this.shulker.getMaxHealth() > 0.25f || this.shulker.distanceToSqr(target) > 16);
        }
        else {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.attackTime = this.baseAttackSpeed;
        this.shulker.setRawPeekAmount(this.shulker.getHealth() / this.shulker.getMaxHealth() > 0.4f ? 100 : 35);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.shulker.setRawPeekAmount(0);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        if (this.shulker.level().getDifficulty() == Difficulty.PEACEFUL)
            return;

        --this.attackTime;
        LivingEntity livingentity = this.shulker.getTarget();
        if (livingentity == null)
            return;

        this.shulker.getLookControl().setLookAt(livingentity, 180.0F, 180.0F);
        double d0 = this.shulker.distanceToSqr(livingentity);
        if (this.shulker.tickCount % 20 == 0 && this.shulker.getHealth() / this.shulker.getMaxHealth() <= 0.5f)
            this.shulker.setRawPeekAmount(35);
        if (d0 < 400.0D) {
            if (this.attackTime <= 0) {
                this.attackTime = this.baseAttackSpeed + this.shulker.getRandom().nextInt(this.bonusHalfSeconds) * 20 / 2;
                this.shulker.level().addFreshEntity(new ShulkerBullet(this.shulker.level(), this.shulker, livingentity, this.shulker.getAttachFace().getAxis()));
                this.shulker.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (this.shulker.getRandom().nextFloat() - this.shulker.getRandom().nextFloat()) * 0.2F + 1.0F);
            }
        }
        else {
            this.shulker.setTarget(null);
        }
    }
}
