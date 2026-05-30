package insane96mcp.enhancedai.modules.mobs.targeting;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class EAINonTameRandomTargetGoal<T extends LivingEntity> extends EAINearestAttackableTarget<T> {
    private final TamableAnimal tamableMob;

    public EAINonTameRandomTargetGoal(TamableAnimal pTamableMob, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(pTamableMob, targetClassIn, checkSight, nearbyOnlyIn, targetingConditions);
        this.tamableMob = pTamableMob;
    }

    public boolean canUse() {
        return !this.tamableMob.isTame() && super.canUse();
    }

    public boolean canContinueToUse() {
        return this.targetMob != null ? this.targetEntitySelector.test(this.mob, this.targetMob) : super.canContinueToUse();
    }
}