package insane96mcp.enhancedai.module.mobs.targeting;

import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

/**
 * Extension of {@link ILNearestAttackableTargetGoal} making use of XRay attribute
 */
public class EAINearestAttackableTarget<T extends LivingEntity> extends ILNearestAttackableTargetGoal<T> {

    public TargetingConditions targetEntitySelectorXRay;

    public int unsensedTicks;

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustReach, TargetingConditions targetingConditions) {
        super(goalOwnerIn, targetClassIn, false, mustReach, null);
        this.targetEntitySelector = targetingConditions;
        this.targetEntitySelectorXRay = targetingConditions.copy().ignoreLineOfSight();
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, ObjTag<EntityType<?>> idTagMatcher, boolean mustReach, TargetingConditions targetingConditions) {
        this(goalOwnerIn, targetClassIn, mustReach, targetingConditions);
        this.targetEntitySelector.selector(living -> idTagMatcher.matches(living.getType()));
        this.targetEntitySelectorXRay.selector(living -> idTagMatcher.matches(living.getType()));
    }

    @Override
    public boolean canUse() {
        int targetChance = Targeting.TARGET_CHANCE.get(this.mob);
        if (targetChance > 0 && this.mob.getRandom().nextInt(targetChance) != 0)
            return false;
        else {
            this.findTarget();
            return this.nearestTarget != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!Targeting.MUST_SENSE_TARGET.get(this.mob))
            return super.canContinueToUse();
        LivingEntity target = this.mob.getTarget();
        if (target == null)
            target = this.targetMob;
        if (target == null)
            return super.canContinueToUse();

        if (this.canSenseTarget(target))
            this.unsensedTicks = 0;
        else if (++this.unsensedTicks > reducedTickDelay(Targeting.UNSENSED_FORGET_TICKS.get(this.mob)))
            return false;

        return super.canContinueToUse();
    }

    public boolean canSenseTarget(LivingEntity target) {
        if (this.mob.getSensing().hasLineOfSight(target))
            return true;
        double xrayDistance = this.getFollowXRayDistance();
        if (xrayDistance > 0d && this.mob.distanceToSqr(target) <= xrayDistance * xrayDistance)
            return true;

        if (Targeting.seeGlowingEntities && target.isCurrentlyGlowing())
            return true;

        return false;
    }

    @Override
    protected void findTarget() {
        // Try normal targeting first
        super.findTarget();

        // Try glowing entities if enabled and no target found
        if (this.nearestTarget == null && Targeting.seeGlowingEntities) {
            tryFindGlowingTarget();
        }

        // Try xray targeting if no target found and xray is available
        if (this.nearestTarget == null && this.getFollowXRayDistance() > 0d) {
            tryFindXRayTarget();
        }
    }

    private void tryFindGlowingTarget() {
        TargetingConditions targetingConditionsGlowing = targetEntitySelector.copy().ignoreLineOfSight().ignoreInvisibilityTesting();
        targetingConditionsGlowing.selector(
            targetEntitySelector.selector != null
                ? targetEntitySelector.selector.and(LivingEntity::isCurrentlyGlowing)
                : LivingEntity::isCurrentlyGlowing
        );
        this.nearestTarget = this.mob.level().getNearestEntity(
            this.targetClass,
            targetingConditionsGlowing,
            this.mob,
            this.mob.getX(),
            this.mob.getEyeY(),
            this.mob.getZ(),
            this.getTargetSearchArea(this.getFollowDistance())
        );
    }

    private void tryFindXRayTarget() {
        this.targetEntitySelectorXRay.range(this.getFollowXRayDistance());

        if (isPlayerTargetClass()) {
            this.nearestTarget = this.mob.level().getNearestPlayer(
                this.targetEntitySelectorXRay,
                this.mob,
                this.mob.getX(),
                this.mob.getEyeY(),
                this.mob.getZ()
            );
        } else {
            this.nearestTarget = this.mob.level().getNearestEntity(
                this.targetClass,
                this.targetEntitySelectorXRay,
                this.mob,
                this.mob.getX(),
                this.mob.getEyeY(),
                this.mob.getZ(),
                this.getTargetSearchArea(this.getFollowXRayDistance())
            );
        }
    }

    private boolean isPlayerTargetClass() {
        return this.targetClass == Player.class || this.targetClass == ServerPlayer.class;
    }

	protected double getFollowXRayDistance() {
        return this.mob.getAttributeValue(EAIAttributes.XRAY_FOLLOW_RANGE);
    }
}
