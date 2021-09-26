package insane96mcp.enhancedai.setup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
	public static void init(final FMLClientSetupEvent event) {
		Minecraft mc = Minecraft.getInstance();

		RenderingRegistry.registerEntityRenderingHandler(EAEntities.THROWN_WEB.get(), (manager) -> new SpriteRenderer<>(manager, mc.getItemRenderer()));
	}
}
