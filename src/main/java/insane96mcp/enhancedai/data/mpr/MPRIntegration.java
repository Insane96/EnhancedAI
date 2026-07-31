package insane96mcp.enhancedai.data.mpr;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.mpr.condition.EAIDataCondition;
import insane96mcp.enhancedai.data.mpr.condition.EAIIsLeaderCondition;
import insane96mcp.enhancedai.data.mpr.property.EAIChangeDataProperty;
import insane96mcp.mobspropertiesrandomness.event.MPRRegisterEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class MPRIntegration {
    @SubscribeEvent
    public void onMPRRegister(MPRRegisterEvent event) {
        if (event.getType() == MPRRegisterEvent.Type.PROPERTY) {
            event.register(EnhancedAI.location("change_data"), EAIChangeDataProperty.class);
        }
        else if (event.getType() == MPRRegisterEvent.Type.CONDITION) {
            event.register(EnhancedAI.location("is_leader"), EAIIsLeaderCondition.class);
            event.register(EnhancedAI.location("data"), EAIDataCondition.class);
        }
    }
}
