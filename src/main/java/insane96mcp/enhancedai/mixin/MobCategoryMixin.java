package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobCategory.class)
public class MobCategoryMixin {
	@Inject(at = @At(value = "RETURN"), method = "getNoDespawnDistance()I", cancellable = true)
	public void getNoDespawnDistance(CallbackInfoReturnable<Integer> callbackInfo) {
		if (Feature.isEnabled(Spawning.class))
			callbackInfo.setReturnValue(Spawning.minMonstersDespawningDistance);
	}
}
