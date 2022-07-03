package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.Modules;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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
	protected PathfinderMob mob;

	@Inject(at = @At(value = "RETURN"), method = "getAttackReachSqr", cancellable = true)
	public void getAttackReachSqr(LivingEntity livingEntity, CallbackInfoReturnable<Double> callbackInfo) {
		if (!Modules.base.attacking.isEnabled() || !Modules.base.attacking.meleeAttacksAttributeBased)
			return;
		double attackRange = this.mob.getAttributeValue(ForgeMod.ATTACK_RANGE.get());
		callbackInfo.setReturnValue(attackRange * attackRange + livingEntity.getBbWidth());
	}
}
