package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonFleeTarget;
import insane96mcp.enhancedai.modules.skeleton.feature.SkeletonShoot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public class AbstractSkeletonMixin {
	@Inject(at = @At("TAIL"), method = "reassessWeaponGoal()V")
	public void reassessWeaponGoal(CallbackInfo callbackInfo) {
		SkeletonShoot.onReassessWeaponGoal((AbstractSkeleton) (Object) this);
		SkeletonFleeTarget.onReassessWeaponGoal((AbstractSkeleton) (Object) this);
	}
}
