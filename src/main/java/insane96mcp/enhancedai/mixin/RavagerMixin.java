package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.enhancedai.modules.illager.RavagerFeature;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ravager.class)
public class RavagerMixin {
	@Definition(id = "block", local = @Local(type = Block.class))
	@Definition(id = "LeavesBlock", type = LeavesBlock.class)
	@Expression("block instanceof LeavesBlock")
	@WrapOperation(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
	public boolean aiStep_onInstanceOf(Object object, Operation<Boolean> original) {
		return object != null && ((Block)object).builtInRegistryHolder().is(RavagerFeature.BREAKABLE_BY_RAVAGER);
	}
}
