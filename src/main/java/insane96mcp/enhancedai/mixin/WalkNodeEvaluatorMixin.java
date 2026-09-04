package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.enhancedai.module.mobs.Pathfinding;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
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

    @ModifyExpressionValue(method = "getPathTypeWithinMobBB", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/WalkNodeEvaluator;getPathType(Lnet/minecraft/world/level/pathfinder/PathfindingContext;III)Lnet/minecraft/world/level/pathfinder/PathType;", ordinal = 2))
    private PathType enhancedai$allowWalkingOnRails(PathType original) {
        if (!Pathfinding.shouldMobsWalkOnRails())
            return original;
        return PathType.RAIL;
    }
}
