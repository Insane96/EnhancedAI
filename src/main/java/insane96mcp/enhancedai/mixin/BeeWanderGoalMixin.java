package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.module.animal.BeeHiveNavigation;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.Bee$BeeWanderGoal")
public class BeeWanderGoalMixin {
	@WrapOperation(method = "findPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Bee;closerThan(Lnet/minecraft/core/BlockPos;I)Z"))
	private boolean enhancedai$wanderHomeDistance(Bee bee, BlockPos pos, int distance, Operation<Boolean> original) {
		if (Feature.isEnabled(BeeHiveNavigation.class))
			distance = BeeHiveNavigation.wanderHomeDistance;
		return original.call(bee, pos, distance);
	}
}
