package insane96mcp.enhancedai.data.mpr.property;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.mobspropertiesrandomness.event.MPRRegisterEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class EAIPropertiesRegistry {
	public static void init() {
		NeoForge.EVENT_BUS.register(new EAIPropertiesRegistry());
	}

	@SubscribeEvent
	public void onMPRRegister(MPRRegisterEvent event) {
		if (event.getType() != MPRRegisterEvent.Type.PROPERTY)
			return;

		event.register(EnhancedAI.location("change_data"), EAIChangeDataProperty.class);
	}
}
