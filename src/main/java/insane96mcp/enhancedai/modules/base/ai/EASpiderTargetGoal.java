package insane96mcp.enhancedai.modules.base.ai;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EASpiderTargetGoal<T extends LivingEntity> extends EANearestAttackableTarget<T> {
    public EASpiderTargetGoal(Spider goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(goalOwnerIn, targetClassIn, checkSight, nearbyOnlyIn, targetPredicate);
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