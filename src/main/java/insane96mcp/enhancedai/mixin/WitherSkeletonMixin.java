package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.skeleton.WitherSkeletons;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherSkeleton.class)
public abstract class WitherSkeletonMixin extends AbstractSkeleton {
	protected WitherSkeletonMixin(EntityType<? extends AbstractSkeleton> p_32133_, Level p_32134_) {
		super(p_32133_, p_32134_);
	}

	@Inject(at = @At(value = "RETURN"), method = "getArrow", cancellable = true)
	public void onGetArrow(CallbackInfoReturnable<AbstractArrow> cir) {
		if (!WitherSkeletons.witherInsteadOfFire())
			return;
		AbstractArrow abstractArrow = cir.getReturnValue();
		abstractArrow.clearFire();
		if (abstractArrow instanceof Arrow arrow) {
			arrow.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
		}
		cir.setReturnValue(abstractArrow);
	}
}
