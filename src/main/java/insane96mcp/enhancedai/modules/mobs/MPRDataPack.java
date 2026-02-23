package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.util.IntegratedPack;
import net.neoforged.fml.ModList;

@LoadFeature(module = EAIModules.Ids.MOBS, name = "MPR data pack", description = "Enables a Mobs Properties Randomness data pack that adds even more buffs to mobs. Only gets enabled if MPR is installed")
public class MPRDataPack extends Feature {

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addServerPack(EnhancedAI.MOD_ID, "mpr_integration", "Enhanced AI -> MPR Integration", () -> isEnabled() && ModList.get().isLoaded("mobspropertiesrandomness"));
	}
}
