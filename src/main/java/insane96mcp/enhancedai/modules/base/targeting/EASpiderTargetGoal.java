package insane96mcp.enhancedai.modules.base.targeting;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Spider;

public class EASpiderTargetGoal<T extends LivingEntity> extends EANearestAttackableTarget<T> {
    public EASpiderTargetGoal(Spider goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(goalOwnerIn, targetClassIn, checkSight, nearbyOnlyIn, targetingConditions);
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        float f = this.mob.getLightLevelDependentMagicValue();
        return !(f >= 0.5F) && super.canUse();
    }
}