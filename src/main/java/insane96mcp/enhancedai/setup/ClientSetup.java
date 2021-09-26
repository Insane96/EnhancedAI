package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EnhancedAI.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
	@SubscribeEvent
	public static void init(final FMLClientSetupEvent event) {
		Minecraft mc = Minecraft.getInstance();

		RenderingRegistry.registerEntityRenderingHandler(EAEntities.THROWN_WEB.get(), (manager) -> new SpriteRenderer<>(manager, mc.getItemRenderer()));
	}
}
