package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.illager.RavagerFeature;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Ravager.class)
public class RavagerMixin {
	@ModifyConstant(method = "aiStep()V", constant = @Constant(classValue = LeavesBlock.class))
	public boolean aiStep_onInstanceOf(Object targetObj, Class<?> classValue) {
		return targetObj != null && ((Block)targetObj).builtInRegistryHolder().is(RavagerFeature.BREAKABLE_BY_RAVAGER);
	}

	/*@ModifyConstant(method = "aiStep()V", constant = @Constant(classValue = LeavesBlock.class))
	public Class<?> aiStep_onInstanceOf(Class<?> constant) {
		return ((Block)targetObj).builtInRegistryHolder().is(RavagerFeature.BREAKABLE_BY_RAVAGER);
	}*/
}
