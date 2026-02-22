package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.modules.mobs.FireImmuneTicks;
import insane96mcp.enhancedai.modules.mobs.PushResistance;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyReturnValue(method = "getFireImmuneTicks", at = @At(value = "RETURN"))
    public int enhancedai$getFireImmuneTicks(int original) {
        if (!Feature.isEnabled(FireImmuneTicks.class)
                || !FireImmuneTicks.FIRE_IMMUNE_TICKS.has((Entity) (Object) this))
            return original;
        return FireImmuneTicks.FIRE_IMMUNE_TICKS.get((Entity) (Object) this);
    }

    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    public void enhancedai$cancelPush(double pX, double pY, double pZ, CallbackInfo ci) {
        double resistance = PushResistance.getPushResistance((Entity) (Object) this);
        if (resistance <= 0d)
            ci.cancel();
    }

    @WrapOperation(method = "push(DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 enhancedai$pushResistance(Vec3 instance, double x, double y, double z, Operation<Vec3> original) {
        double resistance = PushResistance.getPushResistance((Entity) (Object) this);
        return original.call(instance, x * resistance, y * resistance, z * resistance);
    }
}
