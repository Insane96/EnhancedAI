package insane96mcp.enhancedai.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Inject(at = @At(value = "TAIL"), method = "render")
	private static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double d1, double d2, double d3, CallbackInfo callbackInfo) {
		if (Minecraft.getInstance().options.renderDebug)
			Minecraft.getInstance().debugRenderer.pathfindingRenderer.render(poseStack, bufferSource, d1, d2, d3);
	}
}
