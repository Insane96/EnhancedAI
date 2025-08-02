package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.modules.creeper.Creeper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.monster.Creeper.class)
public class CreeperMixin extends Monster {
	protected CreeperMixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
		super(p_33002_, p_33003_);
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), method = "tick()V")
	public void tickOnPlaySound(net.minecraft.world.entity.monster.Creeper instance, SoundEvent soundEvent, float volume, float pitch, Operation<Void> original) {
		Creeper.FuseExplodeSounds fuseExplodeSounds = Creeper.FuseExplodeSounds.get(instance);
		if (fuseExplodeSounds == Creeper.FuseExplodeSounds.NONE) {
			original.call(instance, soundEvent, volume, pitch);
			return;
		}
        //noinspection DataFlowIssue
        this.playSound(fuseExplodeSounds.fuse.get(), volume, 1f);
	}

	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	public void causeFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (Creeper.DISABLE_FALLING_SWELLING.get((net.minecraft.world.entity.monster.Creeper) (Object) this))
			cir.setReturnValue(super.causeFallDamage(distance, damageMultiplier, source));
	}
}
