package insane96mcp.enhancedai.module.witch.alliedmonsters;

import insane96mcp.enhancedai.module.mobs.targeting.EAINearestAttackableTarget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class WitchTargetMonsterGoal<T extends LivingEntity> extends EAINearestAttackableTarget<T> {
    int cooldown;

    public WitchTargetMonsterGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustReach, TargetingConditions targetingConditions) {
        super(goalOwnerIn, targetClassIn, mustReach, targetingConditions);
    }

    @Override
    public boolean canUse() {
        if (--this.cooldown > 0)
            return false;
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return --this.cooldown > 0 && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.cooldown = this.adjustedTickDelay(200);
    }

    @Override
    public void tick() {
        super.tick();
        --this.cooldown;
    }

    @Override
    public void stop() {
        this.cooldown = this.adjustedTickDelay(600);
        super.stop();
    }
}
