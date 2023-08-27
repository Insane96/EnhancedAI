package insane96mcp.enhancedai.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HurtByTargetGoal.class)
public class HurtByTargetGoalMixin extends TargetGoal {

	public HurtByTargetGoalMixin(Mob p_26140_, boolean p_26141_) {
		super(p_26140_, p_26141_);
	}

	@Inject(at = @At(value = "HEAD"), method = "canUse", cancellable = true)
	public void onCanUseCheck(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity lastHurtByMob = this.mob.getLastHurtByMob();
		if (lastHurtByMob != null && this.mob.getTarget() == lastHurtByMob)
			cir.setReturnValue(false);
	}

	@Override
	@Shadow
	public boolean canUse() { return false; }
}
