package insane96mcp.enhancedai.data.mpr.condition;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;

import static insane96mcp.mobspropertiesrandomness.data.json.condition.ConditionsRegistry.CONDITIONS;

public class EAIConditionsRegistry {
	/// Use you own namespace
	private static void register(String id, Class<? extends MPRCondition> clazz) {
		CONDITIONS.put(EnhancedAI.location(id), clazz);
	}

	public static void init() {
		register("is_leader", EAIIsLeaderCondition.class);
		register("data", EAIDataCondition.class);
	}
}
