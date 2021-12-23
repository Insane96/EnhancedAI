package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.Modules;
import net.minecraft.entity.EntityClassification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityClassification.class)
public class EntityClassificationMixin {
	@Inject(at = @At(value = "RETURN"), method = "getNoDespawnDistance()I", cancellable = true)
	public void tickOnPlaySound(CallbackInfoReturnable<Integer> callbackInfo) {
		callbackInfo.setReturnValue(Modules.base.base.minMonstersDespawningDistance);
	}
}
