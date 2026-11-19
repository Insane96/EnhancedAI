package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.module.animal.BeeHiveNavigation;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bee.class)
public class BeeMixin {
	@WrapOperation(method = "isHiveValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Bee;isTooFarAway(Lnet/minecraft/core/BlockPos;)Z"))
	private boolean enhancedai$hiveForgetDistance(Bee bee, BlockPos pos, Operation<Boolean> original) {
		if (Feature.isEnabled(BeeHiveNavigation.class))
			return BeeHiveNavigation.isHiveTooFarAway(bee, pos);
		return original.call(bee, pos);
	}
}
