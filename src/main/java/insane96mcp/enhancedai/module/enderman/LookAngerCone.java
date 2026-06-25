package insane96mcp.enhancedai.module.enderman;

import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;

@LoadFeature(module = EAIModules.ENDERMAN, description = "Changes the angular size of the enderman's 'face' that angers them when looked at. Higher values make endermen anger from a wider viewing angle.")
public class LookAngerCone extends Feature {
	@Config(min = 0d, max = 1d, description = "Controls how directly a player must look at an enderman to anger it. Vanilla is 0.025. Higher values make endermen easier to anger (wider cone).")
	public static Double lookAngerCone = 0.07d;
}