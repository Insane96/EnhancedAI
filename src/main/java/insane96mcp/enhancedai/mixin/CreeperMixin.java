package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.creeper.CreeperSwell;
import insane96mcp.enhancedai.setup.EASounds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public class CreeperMixin extends Monster {
	protected CreeperMixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
		super(p_33002_, p_33003_);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), method = "tick()V")
	public void tickOnPlaySound(CallbackInfo callbackInfo) {
		Creeper $this = (Creeper) (Object) this;
		if ($this.getPersistentData().getBoolean(CreeperSwell.CENA))
			$this.playSound(EASounds.CREEPER_CENA_FUSE.get(), 5.0f, 1.0f);
	}

	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	public void causeFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (CreeperSwell.shouldDisableFallingSwelling())
			cir.setReturnValue(super.causeFallDamage(distance, damageMultiplier, source));
	}
}
