package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.enhancedai.module.mobs.Pathfinding;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GroundPathNavigation.class)
public class GroundPathNavigationMixin {
    @ModifyExpressionValue(method = "createPath(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;blockPosition()Lnet/minecraft/core/BlockPos;"))
	public BlockPos enhancedai$standingBlockPos(BlockPos original, Entity pEntity, int pAccuracy) {
        if (!Pathfinding.shouldPathfindToStandingPosition()
                || !(pEntity instanceof LivingEntity livingEntity))
            return original;
        return livingEntity.getOnPos();
    }
}
