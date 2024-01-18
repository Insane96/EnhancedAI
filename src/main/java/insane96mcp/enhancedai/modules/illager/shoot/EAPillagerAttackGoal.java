package insane96mcp.enhancedai.modules.illager.shoot;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;

public class EAPillagerAttackGoal extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final Pillager mob;
    private CrossbowState crossbowState;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    private int attackCooldown;
    private float inaccuracy;

    public EAPillagerAttackGoal(Pillager pMob, double pSpeedModifier, float pAttackRadius, int attackCooldown, float inaccuracy) {
        this.crossbowState = CrossbowState.UNCHARGED;
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.attackCooldown = attackCooldown;
        this.inaccuracy = inaccuracy;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding((is) -> is.getItem() instanceof CrossbowItem);
    }

    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            CrossbowItem.setCharged(this.mob.getUseItem(), false);
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
        boolean isOutOfRangeOrCantSee = (distance > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
        if (isOutOfRangeOrCantSee) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
            }
        }
        else if (this.crossbowState == CrossbowState.CHARGED){
            this.updatePathDelay = 0;
            this.mob.getNavigation().stop();
        }

        this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
        if (this.crossbowState == CrossbowState.UNCHARGED && !isOutOfRangeOrCantSee) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
            this.crossbowState = CrossbowState.CHARGING;
            this.mob.setChargingCrossbow(true);
        }
        else if (this.crossbowState == CrossbowState.CHARGING) {
            if (!this.mob.isUsingItem())
                this.crossbowState = CrossbowState.UNCHARGED;

            int useTicks = this.mob.getTicksUsingItem();
            ItemStack crossbow = this.mob.getUseItem();
            if (useTicks >= CrossbowItem.getChargeDuration(crossbow)) {
                this.mob.releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = this.attackCooldown;
                this.mob.setChargingCrossbow(false);
            }
        }
        else if (this.crossbowState == CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowState.READY_TO_ATTACK;
            }
        }
        else if (this.crossbowState == CrossbowState.READY_TO_ATTACK && hasLineOfSight) {
            this.performCrossbowAttack();
            ItemStack crossbow = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
            CrossbowItem.setCharged(crossbow, false);
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
            performShooting(this.mob.level(), this.mob, interactionhand, itemstack, this.inaccuracy);
        }

        this.mob.onCrossbowAttackPerformed();
    }

    public static void performShooting(Level pLevel, LivingEntity pShooter, InteractionHand pUsedHand, ItemStack crossbowStack, float inaccuracy) {
        if (pShooter instanceof Player player && net.minecraftforge.event.ForgeEventFactory.onArrowLoose(crossbowStack, pShooter.level(), player, 1, true) < 0)
            return;
        List<ItemStack> list = CrossbowItem.getChargedProjectiles(crossbowStack);
        float[] afloat = getShotPitches(pShooter.getRandom());

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            boolean flag = pShooter instanceof Player && ((Player) pShooter).getAbilities().instabuild;
            if (!itemstack.isEmpty()) {
                if (i == 0) {
                    shootProjectile(pLevel, pShooter, pUsedHand, crossbowStack, itemstack, afloat[i], flag, inaccuracy, 0.0F);
                }
                else if (i == 1) {
                    shootProjectile(pLevel, pShooter, pUsedHand, crossbowStack, itemstack, afloat[i], flag, inaccuracy, -10.0F);
                }
                else if (i == 2) {
                    shootProjectile(pLevel, pShooter, pUsedHand, crossbowStack, itemstack, afloat[i], flag, inaccuracy, 10.0F);
                }
            }
        }

        CrossbowItem.onCrossbowShot(pLevel, pShooter, crossbowStack);
    }

    private static float[] getShotPitches(RandomSource pRandom) {
        boolean flag = pRandom.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, pRandom), getRandomShotPitch(!flag, pRandom)};
    }

    private static float getRandomShotPitch(boolean pIsHighPitched, RandomSource pRandom) {
        float f = pIsHighPitched ? 0.63F : 0.43F;
        return 1.0F / (pRandom.nextFloat() * 0.5F + 1.8F) + f;
    }

    private static void shootProjectile(Level pLevel, LivingEntity pShooter, InteractionHand pHand, ItemStack pCrossbowStack, ItemStack pAmmoStack, float pSoundPitch, boolean pIsCreativeMode, float inaccuracy, float pProjectileAngle) {
        if (!pLevel.isClientSide) {
            boolean isShootingFirework = pAmmoStack.is(Items.FIREWORK_ROCKET);
            Projectile projectile;
            if (isShootingFirework) {
                projectile = new FireworkRocketEntity(pLevel, pAmmoStack, pShooter, pShooter.getX(), pShooter.getEyeY() - (double) 0.15F, pShooter.getZ(), true);
            }
            else {
                projectile = CrossbowItem.getArrow(pLevel, pShooter, pCrossbowStack, pAmmoStack);
                if (pIsCreativeMode || pProjectileAngle != 0.0F) {
                    ((AbstractArrow) projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            if (pShooter instanceof CrossbowAttackMob mob) {
                attackEntityWithRangedAttack(pShooter, mob.getTarget(), pCrossbowStack, projectile, pProjectileAngle, inaccuracy);
            }

            pCrossbowStack.hurtAndBreak(isShootingFirework ? 3 : 1, pShooter, entity -> entity.broadcastBreakEvent(pHand));
            pLevel.addFreshEntity(projectile);
            pLevel.playSound(null, pShooter.getX(), pShooter.getY(), pShooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pSoundPitch);
        }
    }

    protected static void attackEntityWithRangedAttack(LivingEntity livingEntity, LivingEntity target, ItemStack crossbow, Projectile projectile, float angle, float inaccuracy) {
        double distance = livingEntity.distanceTo(target);
        float distanceY = (float) (target.getY() - livingEntity.getY());
        float dirX = (float) (target.getX() - livingEntity.getX());
        float dirZ = (float) (target.getZ() - livingEntity.getZ());
        float distanceXZ = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
        float yPos = (float) target.getY(0d);
        yPos += target.getEyeHeight() * 0.5f;
        if (distanceXZ != 0f)
            yPos += distanceY / distanceXZ;
        float dirY = (float) (yPos - projectile.getY());
        Vector3f shootRotation = ((CrossbowAttackMob) livingEntity).getProjectileShotVector(livingEntity, new Vec3(dirX, dirY + distanceXZ * 0.19f, dirZ), angle);
        projectile.shoot(shootRotation.x(), shootRotation.y(), shootRotation.z(), 1.1f + ((float) distance / 32f) + (float) Math.max(distanceY / 48d, 0f), inaccuracy);
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
