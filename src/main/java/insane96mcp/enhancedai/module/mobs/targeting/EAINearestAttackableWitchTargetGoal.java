package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.raid.Raider;

/**
 * Keeps using the replaced vanilla goal's canAttack, as it's the one Witch#aiStep toggles
 */
public class EAINearestAttackableWitchTargetGoal<T extends LivingEntity> extends EAINearestAttackableTarget<T> {
    private final NearestAttackableWitchTargetGoal<?> vanillaGoal;

    public EAINearestAttackableWitchTargetGoal(Raider goalOwnerIn, Class<T> targetClassIn, boolean nearbyOnlyIn, TargetingConditions targetingConditions, NearestAttackableWitchTargetGoal<?> vanillaGoal) {
        super(goalOwnerIn, targetClassIn, nearbyOnlyIn, targetingConditions);
        this.vanillaGoal = vanillaGoal;
    }

    public boolean canUse() {
        return this.vanillaGoal.canAttack && super.canUse();
    }
}
