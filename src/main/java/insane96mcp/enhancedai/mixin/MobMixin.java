package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.mobs.Attacking;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobMixin {
    @Inject(at = @At(value = "HEAD"), method = "isWithinMeleeAttackRange", cancellable = true)
    private void changeIsWithinMeleeAttackRange(LivingEntity attacked, CallbackInfoReturnable<Boolean> cir) {
        if (!Feature.isEnabled(Attacking.class))
            return;
        cir.setReturnValue(Attacking.isWithinMeleeAttackRange((Mob) (Object) this, attacked));
    }
}
