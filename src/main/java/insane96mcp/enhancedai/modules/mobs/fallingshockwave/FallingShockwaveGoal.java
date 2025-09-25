package insane96mcp.enhancedai.modules.mobs.fallingshockwave;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class FallingShockwaveGoal extends Goal {
    Mob owner;
    int cooldown;

    public FallingShockwaveGoal(Mob owner) {
        this.owner = owner;
        this.cooldown = this.adjustedTickDelay(FallingShockwave.JUMP_COOLDOWN.get(owner));
    }

    @Override
    public boolean canUse() {
        if (!this.owner.onGround()
                || FallingShockwave.DAMAGE_PER_BLOCK.get(this.owner) <= 0)
            return false;
        LivingEntity target = this.owner.getTarget();
        return --this.cooldown <= 0 && target != null && this.owner.getSensing().hasLineOfSight(target) && this.owner.distanceToSqr(target) <= 16f;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        this.owner.setDeltaMovement(this.owner.getDeltaMovement().add(0, FallingShockwave.JUMP_STRENGTH.get(this.owner), 0));
        this.owner.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void stop() {
        this.cooldown = this.adjustedTickDelay(FallingShockwave.JUMP_COOLDOWN.get(owner));
    }
}
