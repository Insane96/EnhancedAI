package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.pets.snowgolem.SnowGolems;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
public class SnowballMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER), method = "onHitEntity")
	private void onHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
		if (Feature.isEnabled(SnowGolems.class))
			entityHitResult.getEntity().invulnerableTime = 0;
	}
}
