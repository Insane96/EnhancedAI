package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.setup.EASounds;
import insane96mcp.enhancedai.setup.Strings;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"), method = "tick()V")
	public void tickOnPlaySound(CallbackInfo callbackInfo) {
		Creeper $this = (Creeper) (Object) this;
		if ($this.getPersistentData().getBoolean(Strings.Tags.Creeper.CENA))
			$this.playSound(EASounds.CREEPER_CENA_FUSE.get(), 5.0f, 1.0f);
	}
}
