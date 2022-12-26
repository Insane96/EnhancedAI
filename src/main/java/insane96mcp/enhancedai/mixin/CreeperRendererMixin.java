package insane96mcp.enhancedai.mixin;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreeperRenderer.class)
public class CreeperRendererMixin {
	/*@Inject(at = @At("HEAD"), method = "scale(Lnet/minecraft/world/entity/monster/Creeper;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", cancellable = true)
	public void onCreeperScale(Creeper creeper, PoseStack poseStack, float partialTicks, CallbackInfo ci) {
		if (CreeperSwell.onCreeperScale(creeper, poseStack, partialTicks))
			ci.cancel();
	}*/
}
