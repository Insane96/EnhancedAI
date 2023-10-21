package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.creeper.CreeperSwell;
import insane96mcp.enhancedai.setup.EASounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public class CreeperMixin extends Monster {
	protected CreeperMixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
		super(p_33002_, p_33003_);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), method = "tick()V")
	public void tickOnPlaySound(Creeper instance, SoundEvent soundEvent, float volume, float pitch) {
		if (this.getPersistentData().getBoolean(CreeperSwell.ANGRY)) {
			if (CreeperSwell.angryCenaSounds) {
				soundEvent = EASounds.CREEPER_CENA_FUSE.get();
				pitch = 1.0f;
			}
			else
				pitch = 0.25f;
			this.playSound(soundEvent, 4.0f, pitch);
		}
		else
			this.playSound(soundEvent, volume, pitch);
	}

	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	public void causeFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (CreeperSwell.shouldDisableFallingSwelling())
			cir.setReturnValue(super.causeFallDamage(distance, damageMultiplier, source));
	}
}
