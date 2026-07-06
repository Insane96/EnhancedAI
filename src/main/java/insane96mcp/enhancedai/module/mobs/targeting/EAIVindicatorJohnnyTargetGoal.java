package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Vindicator;

public class EAIVindicatorJohnnyTargetGoal extends EAINearestAttackableTarget<LivingEntity> {
    public EAIVindicatorJohnnyTargetGoal(Vindicator goalOwnerIn, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(goalOwnerIn, LivingEntity.class, nearbyOnlyIn, targetingConditions);
    }

    public boolean canUse() {
        return ((Vindicator)this.mob).isJohnny && super.canUse();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        super.start();
        this.mob.setNoActionTime(0);
    }
}