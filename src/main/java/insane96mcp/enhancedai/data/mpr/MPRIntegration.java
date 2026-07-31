package insane96mcp.enhancedai.data.mpr;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.mpr.condition.EAIDataCondition;
import insane96mcp.enhancedai.data.mpr.condition.EAIIsLeaderCondition;
import insane96mcp.enhancedai.data.mpr.property.EAIChangeDataProperty;
import insane96mcp.mobspropertiesrandomness.data.json.condition.ConditionsRegistry;
import insane96mcp.mobspropertiesrandomness.data.json.property.PropertiesRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public class MPRIntegration {
    public MPRIntegration(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegister);
    }

    private void onRegister(RegisterEvent event) {
        event.register(ConditionsRegistry.REGISTRY_KEY, helper -> {
            helper.register(EnhancedAI.location("is_leader"), EAIIsLeaderCondition.class);
            helper.register(EnhancedAI.location("data"), EAIDataCondition.class);
        });
        event.register(PropertiesRegistry.REGISTRY_KEY, helper ->
                helper.register(EnhancedAI.location("change_data"), EAIChangeDataProperty.class));
    }
}
