package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.enhancedai.module.warden.WardenListenRange;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Warden.VibrationUser.class)
public abstract class Warden$VibrationUserMixin {
	@Shadow
	@Final
	Warden this$0;

	@ModifyReturnValue(method = "getListenerRadius", at = @At("RETURN"))
    public int enhancedai$wardenListenRadius(int original) {
        return WardenListenRange.changeRange(this.this$0, original);
    }
}
