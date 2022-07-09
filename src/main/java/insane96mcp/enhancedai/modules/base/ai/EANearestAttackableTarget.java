package insane96mcp.enhancedai.modules.base.ai;

import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Extension of ILNearestAttackableTargetGoal making use of XRay attribute
 */
public class EANearestAttackableTarget<T extends LivingEntity> extends ILNearestAttackableTargetGoal<T> {

    public TargetingConditions targetEntitySelectorXRay;

    public EANearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(goalOwnerIn, targetClassIn, mustSee, mustReach, targetPredicate);
        this.targetEntitySelectorXRay = TargetingConditions.forCombat().ignoreLineOfSight().range(this.getFollowXRayDistance()).selector(targetPredicate);
    }

    @Override
    protected void findTarget() {
        if (this.targetClass != Player.class && this.targetClass != ServerPlayer.class) {
            //Try to find the nearest player without xray, then try with xray if the attribute is not 0
            this.nearestTarget = this.mob.level.getNearestEntity(this.targetClass, this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetSearchArea(this.getFollowDistance()));
            if (this.nearestTarget == null && this.getFollowXRayDistance() > 0d) {
                this.nearestTarget = this.mob.level.getNearestEntity(this.targetClass, this.targetEntitySelectorXRay, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetSearchArea(this.getFollowXRayDistance()));
            }
        }
        else {
            //Try to find the nearest player without xray, then try with xray if the attribute is not 0
            this.nearestTarget = this.mob.level.getNearestPlayer(this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            if (this.nearestTarget == null && this.getFollowXRayDistance() > 0d) {
                this.nearestTarget = this.mob.level.getNearestPlayer(this.targetEntitySelectorXRay, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }
        }
    }

    protected double getFollowXRayDistance() {
        return this.mob.getAttributeValue(EAAttributes.XRAY_FOLLOW_RANGE.get());
    }
}
