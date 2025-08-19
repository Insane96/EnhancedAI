package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.enhancedai.modules.mobs.movement.Movement;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomStrollGoal.class)
public class RandomStrollGoalMixin {
	@Shadow
	@Final
	protected PathfinderMob mob;

	@ModifyExpressionValue(method = "canUse", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/RandomStrollGoal;interval:I"))
	public int enhancedai$interval(int original) {
		if (!Movement.RANDOM_STROLL_CHANCE_MULTIPLIER.has(this.mob))
			return original;
		return (int) (original * Movement.RANDOM_STROLL_CHANCE_MULTIPLIER.get(this.mob));
	}

	@ModifyExpressionValue(method = "canUse", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/RandomStrollGoal;checkNoActionTime:Z"))
	public boolean enhancedai$interval(boolean original) {
		if (!Feature.isEnabled(Movement.class)
				 || !Movement.allowRandomStrollAwayFromPlayer)
			return original;
		return false;
	}
}
