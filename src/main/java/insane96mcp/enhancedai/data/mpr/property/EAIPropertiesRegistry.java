package insane96mcp.enhancedai.data.mpr.property;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.mobspropertiesrandomness.data.json.property.MPRProperty;

import static insane96mcp.mobspropertiesrandomness.data.json.property.PropertiesRegistry.PROPERTIES;

public class EAIPropertiesRegistry {
	/// Use you own namespace
	private static void register(String id, Class<? extends MPRProperty> clazz) {
		PROPERTIES.put(EnhancedAI.location(id), clazz);
	}

	public static void init() {
		register("change_data", EAIChangeDataProperty.class);
	}
}
