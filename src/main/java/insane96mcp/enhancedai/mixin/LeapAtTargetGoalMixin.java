package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.enhancedai.module.mobs.BetterLeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LeapAtTargetGoal.class)
public class LeapAtTargetGoalMixin {
	@ModifyExpressionValue(method = "start", at = @At(value = "CONSTANT", args = "doubleValue=0.4"))
	private double enhancedai$distanceSpeed(double original) {
		if (!BetterLeapAtTargetGoal.shouldUseFeature())
			return original;
		return BetterLeapAtTargetGoal.distanceSpeedMultiplier;
	}

	@ModifyExpressionValue(method = "start", at = @At(value = "CONSTANT", args = "doubleValue=0.2"))
	private double enhancedai$deltaMovementSpeed(double original) {
		if (!BetterLeapAtTargetGoal.shouldUseFeature())
			return original;
		return BetterLeapAtTargetGoal.deltaMovementSpeedMultiplier;
	}
}
