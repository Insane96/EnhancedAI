package insane96mcp.enhancedai.modules.illager.shoot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class EAIPillagerAttackGoal extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final Pillager mob;
    private CrossbowState crossbowState;
    private final double speedModifier;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public EAIPillagerAttackGoal(Pillager pMob, double pSpeedModifier) {
        this.crossbowState = CrossbowState.UNCHARGED;
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding((is) -> is.getItem() instanceof CrossbowItem);
    }

    public boolean canContinueToUse() {
        return this.canUse() || !this.mob.getNavigation().isDone();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        //this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            this.mob.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null)
            return;

        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(livingentity);
        boolean hasSeenRecently = this.seeTime > 0;
        if (hasLineOfSight != hasSeenRecently) {
            this.seeTime = 0;
        }

        if (hasLineOfSight)
            ++this.seeTime;
        else
            --this.seeTime;

        double distance = this.mob.distanceToSqr(livingentity);
        double attackRadius = PillagerShoot.SHOOTING_RANGE.get(this.mob);
        boolean isOutOfRangeOrCantSee = (distance > attackRadius * attackRadius || this.seeTime < 5) && this.attackDelay == 0;
        if (isOutOfRangeOrCantSee) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
            }
        }
        else if (this.crossbowState == CrossbowState.CHARGED){
            this.updatePathDelay = 0;
            //this.mob.getNavigation().stop();
        }

        this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
        if (this.crossbowState == CrossbowState.UNCHARGED && !isOutOfRangeOrCantSee) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
            this.crossbowState = CrossbowState.CHARGING;
            this.mob.setChargingCrossbow(true);
            if (!this.mob.isPassenger()) {
                Entity entity = PillagerShoot.mightHitAnAlly(this.mob, distance);
				if (entity != null)
                	PillagerShoot.tryReposition(this.mob, entity, 2);
            }
        }
        else if (this.crossbowState == CrossbowState.CHARGING) {
            if (!this.mob.isUsingItem())
                this.crossbowState = CrossbowState.UNCHARGED;

            int useTicks = this.mob.getTicksUsingItem();
            ItemStack crossbow = this.mob.getUseItem();
            if (useTicks >= CrossbowItem.getChargeDuration(crossbow, this.mob)) {
                this.mob.releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = PillagerShoot.SHOOTING_COOLDOWN.get(this.mob);
                this.mob.setChargingCrossbow(false);
            }
        }
        else if (this.crossbowState == CrossbowState.CHARGED) {
            if (--this.attackDelay <= 0) {
                Entity entity = PillagerShoot.mightHitAnAlly(this.mob, distance);
                if (entity != null) {
                    this.attackDelay = 20;
                    PillagerShoot.tryReposition(this.mob, entity, 2);
                }
                else
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        }
        else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && hasLineOfSight) {
            this.performCrossbowAttack();
            this.crossbowState = CrossbowState.UNCHARGED;
        }

    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }

    private void performCrossbowAttack() {
        InteractionHand interactionhand = ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem);
        ItemStack itemstack = this.mob.getItemInHand(interactionhand);
        if (this.mob.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
            performShooting(this.mob.level(), this.mob, interactionhand, itemstack, PillagerShoot.INACCURACY.get(this.mob).floatValue(), this.mob.getTarget());
        }

        this.mob.onCrossbowAttackPerformed();
    }

    public static void performShooting(Level pLevel, LivingEntity pShooter, InteractionHand pUsedHand, ItemStack crossbowStack, float inaccuracy, @javax.annotation.Nullable LivingEntity target) {
        float velocity = CrossbowItem.MOB_ARROW_POWER;
        if (target != null) {
            double distance = pShooter.distanceTo(target);
            double distanceY = target.getY() - pShooter.getY();
            velocity = 1.1f + ((float) distance / 32f) + (float) Math.max(distanceY / 48d, 0f);
        }
        ((CrossbowItem) crossbowStack.getItem()).performShooting(pLevel, pShooter, pUsedHand, crossbowStack, velocity, inaccuracy, target);
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
