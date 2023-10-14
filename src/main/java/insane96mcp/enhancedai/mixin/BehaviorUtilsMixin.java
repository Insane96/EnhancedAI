package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.base.feature.Attacking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BehaviorUtils.class)
public abstract class BehaviorUtilsMixin {
	@Inject(at = @At(value = "HEAD"), method = "isWithinMeleeAttackRange", cancellable = true)
	private static void changeIsWithinMeleeAttackRange(Mob attacker, LivingEntity attacked, CallbackInfoReturnable<Boolean> cir) {
		if (!Modules.base.attacking.isEnabled())
			return;
		cir.setReturnValue(Attacking.isWithinMeleeAttackRange(attacker, attacked));
	}
}
