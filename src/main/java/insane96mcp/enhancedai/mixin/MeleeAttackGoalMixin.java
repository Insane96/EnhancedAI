package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.base.feature.Attacking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
	@Final
	@Shadow
	private double speedModifier;

	@Shadow
	private Path path;

	@Final
	@Shadow
	protected PathfinderMob mob;

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
}
