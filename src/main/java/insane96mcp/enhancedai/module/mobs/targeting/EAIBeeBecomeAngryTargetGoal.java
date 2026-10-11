package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;

public class EAIBeeBecomeAngryTargetGoal extends EAINearestAttackableTarget<Player> {
    public EAIBeeBecomeAngryTargetGoal(Bee goalOwnerIn, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(goalOwnerIn, Player.class, nearbyOnlyIn, targetingConditions);
    }

    public boolean canUse() {
        return this.beeCanTarget() && super.canUse();
    }

    public boolean canContinueToUse() {
        if (this.beeCanTarget() && this.mob.getTarget() != null)
            return super.canContinueToUse();
        this.targetMob = null;
        return false;
    }

    private boolean beeCanTarget() {
        Bee bee = (Bee) this.mob;
        return bee.isAngry() && !bee.hasStung();
    }
}
