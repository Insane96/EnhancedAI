package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.drowned.Drowned;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.monster.Drowned.DrownedGoToWaterGoal.class)
public abstract class DrownedGoToWaterGoalMixin {
    @Shadow @Final private PathfinderMob mob;

    @Inject(method = "canUse", at = @At(value = "HEAD"), cancellable = true)
    public void enhancedai$canUse(CallbackInfoReturnable<Boolean> cir) {
        if (!Drowned.sunResistant())
            return;
        if (ModNBTData.get(this.mob, Drowned.FIRE_RESISTANCE_TIME, Integer.class) < 600)
            cir.setReturnValue(false);
    }
}
