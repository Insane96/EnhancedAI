package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.modules.slime.SlimeAttackFix;
import insane96mcp.enhancedai.modules.slime.SlimeJumpDelay;
import insane96mcp.enhancedai.modules.slime.SlimeSize;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slime.class)
public abstract class SlimeMixin extends Mob {

    @Shadow public abstract EntityType<? extends Slime> getType();

    @Shadow
    protected abstract boolean isDealsDamage();

    @Shadow
    protected abstract void dealDamage(LivingEntity pLivingEntity);

    @Shadow
    private boolean wasOnGround;

    protected SlimeMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyArg(method = "finalizeSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;setSize(IZ)V", ordinal = 0))
    public int onFinalizeSpawnSetSize(int size) {
        if (!SlimeSize.shouldOverrideSpawnSize((Slime) (Object) this)
                || !this.getType().is(SlimeSize.AFFECTED_ENTITY_TYPES))
            return size;

        return this.getRandom().nextInt(SlimeSize.maxSpawnSize + 1);
    }

    @Inject(method = "getJumpDelay", at = @At("RETURN"), cancellable = true)
    public void onJumpDelay(CallbackInfoReturnable<Integer> cir) {
        if (!Feature.isEnabled(SlimeJumpDelay.class))
            return;

        cir.setReturnValue(Mth.nextInt(this.random, SlimeJumpDelay.JUMP_DELAY_MIN.get(this), SlimeJumpDelay.JUMP_DELAY_MAX.get(this)));
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    public void onPlayerTouch(Player player, CallbackInfo ci) {
        if (!Feature.isEnabled(SlimeAttackFix.class))
            return;
        ci.cancel();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;tick()V"))
    public void onTick(Slime instance, Operation<Void> original) {
        original.call(instance);
        if (!Feature.isEnabled(SlimeAttackFix.class)
                || this.getTarget() == null
                || !this.isWithinMeleeAttackRange(this.getTarget())
                || !this.isDealsDamage()
                || this.onGround() == this.wasOnGround)
            return;

        this.dealDamage(this.getTarget());
    }

    @WrapOperation(method = "dealDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    public double onDealDamage(Slime instance, Entity entity, Operation<Double> original) {
        if (!Feature.isEnabled(SlimeAttackFix.class))
            return original.call(instance, entity);
        return 0d;
    }
}
