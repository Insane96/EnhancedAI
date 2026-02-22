package insane96mcp.enhancedai.modules.witch.throwing;

import insane96mcp.enhancedai.data.PotionOrMobEffect;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class WitchThrowPotionGoal extends Goal {
    private final Witch witch;

    private LivingEntity target;

    private int attackTime;

    public WitchThrowPotionGoal(Witch witch) {
        this.witch = witch;
        this.attackTime = this.adjustedTickDelay(WitchPotionThrowing.ATTACK_COOLDOWN.get(this.witch));
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
		if (this.witch.isDrinkingPotion())
			return false;
		this.target = this.witch.getTarget();
		return this.target != null
				&& !this.target.isDeadOrDying();
	}

    @Override
    public void stop() {
        this.target = null;
    }

    /*@Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }*/

    @Override
    public void tick() {
        double distanceToTarget = this.witch.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean canSee = this.witch.getSensing().hasLineOfSight(this.target);

        if (!canSee)
            return;

		int maxDistance = WitchPotionThrowing.ATTACK_RANGE.get(this.witch);
		maxDistance *= maxDistance;
        if (distanceToTarget > maxDistance) {
            this.witch.getNavigation().moveTo(this.target, 1d);
            return;
        }

        this.witch.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (--this.attackTime <= 0) {
            this.witch.getNavigation().stop();
            this.throwPotionAtTarget();
			this.attackTime = this.adjustedTickDelay(WitchPotionThrowing.ATTACK_COOLDOWN.get(this.witch));
        }
    }

    private void throwPotionAtTarget() {
        List<PotionOrMobEffect> listToLoop = WitchPotionThrowing.goodPotionsList;
		if (this.target instanceof Player)
			listToLoop = WitchPotionThrowing.badPotionsList;
		boolean apprentice = WitchPotionThrowing.APPRENTICE.get(this.witch);
		if (apprentice) {
			listToLoop = new ArrayList<>(WitchPotionThrowing.badPotionsList);
			listToLoop.addAll(WitchPotionThrowing.goodPotionsList);
		}

        PotionOrMobEffect potionOrMobEffect = null;
        if (apprentice) {
            potionOrMobEffect = listToLoop.get(witch.getRandom().nextInt(listToLoop.size()));
        }
        else {
            for (PotionOrMobEffect pmb : listToLoop) {
                if (pmb.hasMobEffect(this.target))
					continue;

                potionOrMobEffect = pmb;
                break;
            }
        }

        if (potionOrMobEffect != null) {
            if (this.target.getType().is(EntityTypeTags.UNDEAD)) {
                if (potionOrMobEffect.getPotion() == Potions.HEALING || potionOrMobEffect.getPotion() == Potions.REGENERATION)
                    potionOrMobEffect = new PotionOrMobEffect(Potions.HARMING);
                else if (potionOrMobEffect.getPotion() == Potions.STRONG_HEALING || potionOrMobEffect.getPotion() == Potions.STRONG_REGENERATION || potionOrMobEffect.getPotion() == Potions.LONG_REGENERATION)
                    potionOrMobEffect = new PotionOrMobEffect(Potions.STRONG_HARMING);
            }
            ThrownPotion thrownpotion = new ThrownPotion(witch.level(), this.witch);
            ItemStack stack = witch.getRandom().nextDouble() < WitchPotionThrowing.LINGERING_CHANCE.get(this.witch) ? potionOrMobEffect.getLingeringPotionStack() : potionOrMobEffect.getSplashPotionStack();
            thrownpotion.setItem(stack);
            thrownpotion.setXRot(thrownpotion.getXRot() + 20.0F);
            double distance = this.witch.distanceTo(target);
            double dirX = this.target.getX() - this.witch.getX();
            double distanceY = this.target.getY() - this.witch.getY();
            double dirZ = this.target.getZ() - this.witch.getZ();
            double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
            double yPos = this.target.getY(0d);
            yPos += this.target.getEyeHeight() * 0.1 + (distanceY / distanceXZ);
            double dirY = yPos - thrownpotion.getY();
            thrownpotion.shoot(dirX, dirY + distanceXZ * 0.18d, dirZ, 1.1f + ((float) distance / 32f) + (float) Math.max(distanceY / 48d, 0f), WitchPotionThrowing.INACCURACY.get(this.witch).floatValue());
            if (!witch.isSilent()) {
                witch.level().playSound(null, witch.getX(), witch.getY(), witch.getZ(), SoundEvents.WITCH_THROW, witch.getSoundSource(), 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
            }
            witch.level().addFreshEntity(thrownpotion);
        }
    }
}
