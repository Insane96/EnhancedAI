package insane96mcp.enhancedai.data.mpr.condition;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.mobspropertiesrandomness.event.MPRRegisterEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class EAIConditionsRegistry {
	public static void init() {
		NeoForge.EVENT_BUS.register(new EAIConditionsRegistry());
	}

	@SubscribeEvent
	public void onMPRRegister(MPRRegisterEvent event) {
		if (event.getType() != MPRRegisterEvent.Type.CONDITION)
			return;

		event.register(EnhancedAI.location("is_leader"), EAIIsLeaderCondition.class);
		event.register(EnhancedAI.location("data"), EAIDataCondition.class);
	}
}
