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
		this(goalOwnerIn, targetClassIn, mustSee, mustReach, targetingConditions, 60);
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions, int forgetTicks) {
        super(goalOwnerIn, targetClassIn, mustSee, mustReach, null);
        this.targetEntitySelector = targetingConditions;
        this.targetEntitySelectorXRay = targetingConditions.copy().ignoreLineOfSight();
        this.unseenMemoryTicks = forgetTicks;
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, IdTagMatcher idTagMatcher, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions) {
        this(goalOwnerIn, targetClassIn, idTagMatcher, mustSee, mustReach, targetingConditions, 60);
    }

    public EAINearestAttackableTarget(Mob goalOwnerIn, Class<T> targetClassIn, IdTagMatcher idTagMatcher, boolean mustSee, boolean mustReach, TargetingConditions targetingConditions, int forgetTicks) {
        this(goalOwnerIn, targetClassIn, mustSee, mustReach, targetingConditions);
        this.targetEntitySelector.selector(idTagMatcher::matchesEntity);
        this.targetEntitySelectorXRay.selector(idTagMatcher::matchesEntity);
        this.unseenMemoryTicks = forgetTicks;
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
    protected void findTarget() {
        // Try normal targeting first
        this.targetEntitySelector.range(this.getFollowDistance());
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
        return this.mob.getAttributeValue(EAIAttributes.XRAY_FOLLOW_RANGE.get());
    }
}
