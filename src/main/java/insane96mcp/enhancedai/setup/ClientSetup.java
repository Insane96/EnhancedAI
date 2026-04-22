package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.mobs.fisher.EAIFishingHookRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = EnhancedAI.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {
	@SubscribeEvent
	public static void init(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EAIEntities.THROWN_WEB.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(EAIEntities.FISHING_HOOK.get(), EAIFishingHookRenderer::new);
	}
}
