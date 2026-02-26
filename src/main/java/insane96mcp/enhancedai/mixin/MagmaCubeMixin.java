package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.module.slime.MagmaCubeSurfSpeed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MagmaCube.class)
public class MagmaCubeMixin extends Slime {

    public MagmaCubeMixin(EntityType<? extends Slime> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @WrapOperation(method = "jumpInLiquidInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/MagmaCube;setDeltaMovement(DDD)V"))
    public void onJumpInLiquidInternal(MagmaCube instance, double x, double y, double z, Operation<Void> original) {
        double multiplier = MagmaCubeSurfSpeed.getSpeedMultiplier((MagmaCube) (Object) this);
        original.call(instance, x * multiplier, y, z * multiplier);
    }
}
