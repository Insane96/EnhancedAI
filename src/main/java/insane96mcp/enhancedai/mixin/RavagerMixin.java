package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.illager.RavagerFeature;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Ravager.class)
public class RavagerMixin {
	@Redirect(method = "aiStep()V", at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/level/block/Block/LeavesBlock", ordinal = 0))
	public boolean aiStep_onInstanceOf(Object targetObj, Class<?> classValue) {
		return ((Block)targetObj).builtInRegistryHolder().is(RavagerFeature.BREAKABLE_BY_RAVAGER);
	}

	/*@ModifyConstant(method = "aiStep()V", constant = @Constant(classValue = LeavesBlock.class))
	public Class<?> aiStep_onInstanceOf(Class<?> constant) {
		return ((Block)targetObj).builtInRegistryHolder().is(RavagerFeature.BREAKABLE_BY_RAVAGER);
	}*/
}
