package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.mobs.fisher.FishingHookRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnhancedAI.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
	@SubscribeEvent
	public static void init(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EAEntities.THROWN_WEB.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(EAEntities.FISHING_HOOK.get(), FishingHookRenderer::new);
	}
}
