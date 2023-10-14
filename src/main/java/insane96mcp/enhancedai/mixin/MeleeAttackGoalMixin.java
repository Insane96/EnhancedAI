package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.base.feature.Attacking;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {

	@Final
	@Shadow
	protected PathfinderMob mob;

	@Shadow protected abstract boolean isTimeToAttack();

	@Shadow protected abstract void resetAttackCooldown();

	@Inject(at = @At(value = "HEAD"), method = "checkAndPerformAttack", cancellable = true)
	public void getAttackReachSqr(LivingEntity attacked, double distanceSqr, CallbackInfo ci) {
		if (!Feature.isEnabled(Attacking.class))
			return;
		if (Attacking.isWithinMeleeAttackRange(this.mob, attacked) && this.isTimeToAttack() && this.mob.getSensing().hasLineOfSight(attacked)) {
			this.resetAttackCooldown();
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(attacked);
		}
		ci.cancel();
	}
}
