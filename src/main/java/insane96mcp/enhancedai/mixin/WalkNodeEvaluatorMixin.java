package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.enhancedai.module.mobs.Pathfinding;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WalkNodeEvaluator.class)
public class WalkNodeEvaluatorMixin {
    @ModifyExpressionValue(method = "getPathTypeFromState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private static boolean enhancedai$preventWalkingOnOpenTrapdoor(boolean original, @Local BlockState blockstate) {
        if (!Pathfinding.shouldFixWalkingOverOpenTrapdoors()
                || !original)
            return original;
        return !blockstate.getValue(TrapDoorBlock.OPEN);
    }
}
