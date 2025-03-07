package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.enhancedai.modules.warden.WardenFeature;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Warden.VibrationUser.class)
public abstract class Warden$VibrationUserMixin {
	@ModifyReturnValue(method = "getListenerRadius", at = @At("RETURN"))
    public int enhancedai$wardenListenRadius(int original) {
        return WardenFeature.increaseListenRange(original);
    }
}
