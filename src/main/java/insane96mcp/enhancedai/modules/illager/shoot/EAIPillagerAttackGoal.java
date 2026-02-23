package insane96mcp.enhancedai.modules.illager.shoot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;

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
            ItemStack crossbow = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, (item) -> item instanceof CrossbowItem));
            crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
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
            performShooting(this.mob.level(), this.mob, interactionhand, itemstack, PillagerShoot.INACCURACY.get(this.mob).floatValue());
        }

        this.mob.onCrossbowAttackPerformed();
    }

    public static void performShooting(Level pLevel, LivingEntity pShooter, InteractionHand pUsedHand, ItemStack crossbowStack, float inaccuracy) {
        //TODO seems wrong
        List<ItemStack> list = crossbowStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).getItems();
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
                ArrowItem arrowItem = pAmmoStack.getItem() instanceof ArrowItem ai ? ai : (ArrowItem) Items.ARROW;
                AbstractArrow arrow = arrowItem.createArrow(pLevel, pAmmoStack, pShooter, pCrossbowStack);
                arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
                if (pIsCreativeMode || pProjectileAngle != 0.0F) {
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                projectile = arrow;
            }

            if (pShooter instanceof CrossbowAttackMob mob) {
                attackEntityWithRangedAttack(pShooter, mob.getTarget(), pCrossbowStack, projectile, pProjectileAngle, inaccuracy);
            }

            pCrossbowStack.hurtAndBreak(isShootingFirework ? 3 : 1, pShooter, pHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
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
        Vector3f shootRotation = getProjectileShotVector(livingEntity, new Vec3(dirX, dirY + distanceXZ * 0.2f, dirZ), angle);
        projectile.shoot(shootRotation.x(), shootRotation.y(), shootRotation.z(), 1.1f + ((float) distance / 32f) + (float) Math.max(distanceY / 48d, 0f), inaccuracy);
    }

    private static Vector3f getProjectileShotVector(LivingEntity shooter, Vec3 distance, float angle) {
        Vector3f vector3f = distance.toVector3f().normalize();
        Vector3f vector3f1 = new Vector3f(vector3f).cross(new Vector3f(0.0F, 1.0F, 0.0F));
        if ((double) vector3f1.lengthSquared() <= 1.0E-7) {
            Vec3 vec3 = shooter.getUpVector(1.0F);
            vector3f1 = new Vector3f(vector3f).cross(vec3.toVector3f());
        }
        Vector3f vector3f2 = new Vector3f(vector3f).rotateAxis((float) (Math.PI / 2), vector3f1.x, vector3f1.y, vector3f1.z);
        return new Vector3f(vector3f).rotateAxis(angle * (float) (Math.PI / 180.0), vector3f2.x, vector3f2.y, vector3f2.z);
    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
