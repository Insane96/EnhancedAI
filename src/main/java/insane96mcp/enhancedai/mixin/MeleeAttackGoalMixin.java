package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.base.feature.Attacking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin extends Goal {
	@Final
	@Shadow
	private double speedModifier;

	@Shadow
	private Path path;

	@Final
	@Shadow
	protected PathfinderMob mob;

	@Shadow private int ticksUntilNextAttack;

	@Inject(at = @At(value = "RETURN"), method = "getAttackReachSqr", cancellable = true)
	public void onGetAttackReachSqr(LivingEntity livingEntity, CallbackInfoReturnable<Double> callbackInfo) {
		if (!Attacking.shouldChangeAttackRange())
			return;
		double attackRange = this.mob.getAttributeValue(ForgeMod.ENTITY_REACH.get());
		callbackInfo.setReturnValue(attackRange * attackRange + livingEntity.getBbWidth());
	}

	@Inject(at = @At(value = "RETURN"), method = "canContinueToUse", cancellable = true)
	public void onCanContinueToUse(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && this.mob.getTarget() != null && this.mob.distanceToSqr(this.mob.getTarget()) < 10) {
			this.path = this.mob.getNavigation().createPath(this.mob.getTarget(), 0);
			if (this.path != null) {
				this.mob.getNavigation().moveTo(this.path, this.speedModifier);
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(at = @At(value = "RETURN"), method = "getAttackInterval", cancellable = true)
	public void onGetAttackCooldown(CallbackInfoReturnable<Integer> cir) {
		if (!Attacking.shouldUseAttackSpeedAttribute())
			return;
		cir.setReturnValue(this.enhancedAI$getTicksUntilNextAttack());
	}

	@Inject(at = @At(value = "HEAD"), method = "resetAttackCooldown", cancellable = true)
	public void onResetAttackCooldown(CallbackInfo ci) {
		if (!Attacking.shouldUseAttackSpeedAttribute())
			return;
		this.ticksUntilNextAttack = this.enhancedAI$getTicksUntilNextAttack();
		ci.cancel();
	}

	@Unique
	private int enhancedAI$getTicksUntilNextAttack() {
		double attackSpeed = this.mob.getAttributeValue(Attributes.ATTACK_SPEED);
		if (attackSpeed <= 0f) {
			AttributeInstance instance = this.mob.getAttribute(Attributes.ATTACK_SPEED);
			if (instance != null) instance.setDirty();
			attackSpeed = this.mob.getAttributeValue(Attributes.ATTACK_SPEED);
		}
		attackSpeed *= Attacking.attackSpeedMultiplier;
		if (attackSpeed > 2f) attackSpeed = 2f;
		return this.adjustedTickDelay((int) (1d / attackSpeed * 20d));
	}
}
