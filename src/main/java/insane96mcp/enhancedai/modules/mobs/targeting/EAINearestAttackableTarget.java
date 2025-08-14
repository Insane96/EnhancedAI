package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

/**
 * Extension of ILNearestAttackableTargetGoal making use of XRay attribute
 */
public class EAINearestAttackableTarget<T extends LivingEntity> extends ILNearestAttackableTargetGoal<T> {

    public TargetingConditions targetEntitySelectorXRay;

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions) {
        super(goalOwnerIn, targetClassIn, mustSee, mustReach, null);
        this.targetEntitySelector = targetingConditions;
        this.targetEntitySelectorXRay = targetingConditions.copy().ignoreLineOfSight();
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions, int forgetTicks) {
        super(goalOwnerIn, targetClassIn, mustSee, mustReach, null);
        this.targetEntitySelector = targetingConditions;
        this.targetEntitySelectorXRay = targetingConditions.copy().ignoreLineOfSight();
        this.unseenMemoryTicks = forgetTicks;
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, IdTagMatcher idTagMatcher, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions) {
        this(goalOwnerIn, targetClassIn, mustSee, mustReach, targetingConditions);
        this.targetEntitySelector.selector(idTagMatcher::matchesEntity);
        this.targetEntitySelectorXRay.selector(idTagMatcher::matchesEntity);
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, IdTagMatcher idTagMatcher, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions, int forgetTicks) {
        this(goalOwnerIn, targetClassIn, mustSee, mustReach, targetingConditions);
        this.targetEntitySelector.selector(idTagMatcher::matchesEntity);
        this.targetEntitySelectorXRay.selector(idTagMatcher::matchesEntity);
        this.unseenMemoryTicks = forgetTicks;
    }

    @Override
    protected void findTarget() {
        this.targetEntitySelector.range(this.getFollowDistance());
        super.findTarget();
        if (this.nearestTarget != null
                || this.getFollowXRayDistance() <= 0d)
            return;
        this.targetEntitySelectorXRay.range(this.getFollowXRayDistance());
        if (this.targetClass != Player.class && this.targetClass != ServerPlayer.class) {
            this.nearestTarget = this.mob.level().getNearestEntity(this.targetClass, this.targetEntitySelectorXRay.range(this.getFollowXRayDistance()), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetSearchArea(this.getFollowXRayDistance()));
        }
        else {
            //Try to find the nearest player without xray, then try with xray if the attribute is not 0
            this.nearestTarget = this.mob.level().getNearestPlayer(this.targetEntitySelectorXRay.range(this.getFollowXRayDistance()), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }
    }

    protected double getFollowXRayDistance() {
        return this.mob.getAttributeValue(EAIAttributes.XRAY_FOLLOW_RANGE.get());
    }
}
