package insane96mcp.enhancedai.modules.pets.ai;

import insane96mcp.enhancedai.modules.base.ai.EARangedAttackGoal;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EARangedSnowGolemAttackGoal extends EARangedAttackGoal<SnowGolem> {

    public EARangedSnowGolemAttackGoal(SnowGolem mob, double moveSpeedAmpIn, float maxAttackDistanceIn) {
        super(mob, moveSpeedAmpIn, maxAttackDistanceIn, false);
    }

    @Override
    protected void attackTick(LivingEntity target, double distanceFromTarget, boolean canSeeTarget) {
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--this.attackTime <= 0 && canSeeTarget) {
            this.mob.stopUsingItem();
            attackEntityWithRangedAttack(this.mob, target, 1);
            this.attackTime = this.attackCooldown;
        }
    }

    @Override
    protected void attackEntityWithRangedAttack(SnowGolem entity, LivingEntity target, int chargeTicks) {
        ItemStack itemstack = entity.getProjectile(entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(entity, item -> item == Items.BOW)));
        double distance = entity.distanceTo(target);
        double distanceY = target.getY() - entity.getY();
        float f = 1; //distanceFactor / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        Snowball snowball = new Snowball(this.mob.level(), this.mob);
        double dirX = target.getX() - entity.getX();
        double dirZ = target.getZ() - entity.getZ();
        double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double yPos = target.getY(0d);
        yPos += target.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
        double dirY = yPos - snowball.getY();
        snowball.shoot(dirX, dirY + distanceXZ * 0.1d, dirZ, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 2);
        this.mob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
        entity.level().addFreshEntity(snowball);
    }
}
