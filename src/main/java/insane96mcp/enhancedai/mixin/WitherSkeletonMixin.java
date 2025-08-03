package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.modules.skeleton.WitherSkeletons;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitherSkeleton.class)
public class WitherSkeletonMixin {
	@WrapOperation(method = "getArrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;setSecondsOnFire(I)V"))
	public void onGetArrow(AbstractArrow instance, int ticks, Operation<Void> original) {
		if (!WitherSkeletons.witherInsteadOfFire()) {
			original.call(instance, ticks);
			return;
		}
		if (instance instanceof Arrow arrow)
			arrow.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
	}
}
