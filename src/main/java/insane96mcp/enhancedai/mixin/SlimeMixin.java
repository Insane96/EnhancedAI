package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.magmacube.Slimes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.class)
public class SlimeMixin extends Mob {

    protected SlimeMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyArg(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;setSize(IZ)V", ordinal = 0))
    public int onFinalizeSpawnSetSize(int size) {
        if (!Slimes.shouldOverrideSpawnSize())
            return size;

        return this.getRandom().nextInt(Slimes.maxSpawnSize + 1);
    }

    @Inject(method = "getJumpDelay", at = @At("RETURN"), cancellable = true)
    public void onJumpDelay(CallbackInfoReturnable<Integer> cir) {
        if (!Slimes.shouldChangeJumpDelay())
            return;

        cir.setReturnValue((int) (cir.getReturnValue() * Slimes.jumpDelayMultiplier));
    }
}
