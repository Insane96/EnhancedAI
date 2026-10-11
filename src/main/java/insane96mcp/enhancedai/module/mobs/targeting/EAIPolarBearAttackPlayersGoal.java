package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;

public class EAIPolarBearAttackPlayersGoal extends EAINearestAttackableTarget<Player> {
    public EAIPolarBearAttackPlayersGoal(PolarBear goalOwnerIn, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(goalOwnerIn, Player.class, nearbyOnlyIn, targetingConditions);
    }

    /**
     * Polar bears attack players only if there's a cub nearby
     */
    public boolean canUse() {
        if (this.mob.isBaby() || !super.canUse())
            return false;
        for (PolarBear polarBear : this.mob.level().getEntitiesOfClass(PolarBear.class, this.mob.getBoundingBox().inflate(8.0D, 4.0D, 8.0D))) {
            if (polarBear.isBaby())
                return true;
        }
        return false;
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance() * 0.5D;
    }

    @Override
    protected double getFollowXRayDistance() {
        return super.getFollowXRayDistance() * 0.5D;
    }
}
