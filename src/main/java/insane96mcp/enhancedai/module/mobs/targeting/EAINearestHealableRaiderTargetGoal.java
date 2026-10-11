package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.raid.Raider;

/**
 * Keeps using the replaced vanilla goal's cooldown, as it's the one Witch#aiStep decrements and checks to allow attacking players
 */
public class EAINearestHealableRaiderTargetGoal<T extends LivingEntity> extends EAINearestAttackableTarget<T> {
    private final NearestHealableRaiderTargetGoal<?> vanillaGoal;

    public EAINearestHealableRaiderTargetGoal(Raider goalOwnerIn, Class<T> targetClassIn, boolean nearbyOnlyIn, TargetingConditions targetingConditions, NearestHealableRaiderTargetGoal<?> vanillaGoal) {
        super(goalOwnerIn, targetClassIn, nearbyOnlyIn, targetingConditions);
        this.vanillaGoal = vanillaGoal;
    }

    public boolean canUse() {
        if (this.vanillaGoal.getCooldown() > 0 || !this.mob.getRandom().nextBoolean())
            return false;
        if (!((Raider) this.mob).hasActiveRaid())
            return false;
        this.findTarget();
        return this.nearestTarget != null;
    }

    public void start() {
        this.vanillaGoal.cooldown = reducedTickDelay(200);
        super.start();
    }
}
