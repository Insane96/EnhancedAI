package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.module.drowned.SunResistantDrowned;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/world/entity/monster/Drowned$DrownedGoToWaterGoal")
public abstract class DrownedGoToWaterGoalMixin {
    @Shadow @Final private PathfinderMob mob;

    @Inject(method = "canUse", at = @At(value = "HEAD"), cancellable = true)
    public void enhancedai$canUse(CallbackInfoReturnable<Boolean> cir) {
        if (!Feature.isEnabled(SunResistantDrowned.class))
            return;
		int sunResistantTicks = SunResistantDrowned.TICKS.get(this.mob);
		if (sunResistantTicks <= 0)
			return;
        if (ModNBTData.get(this.mob, SunResistantDrowned.TIME, Integer.class) < sunResistantTicks)
            cir.setReturnValue(false);
    }
}
