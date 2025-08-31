package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.IntegratedPack;
import net.minecraftforge.fml.ModList;

@LoadFeature(module = Modules.Ids.MOBS, name = "MPR data pack", description = "Enables a Mobs Properties Randomness data pack that adds even more buffs to mobs.")
public class MPRDataPack extends Feature {

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addServerPack(EnhancedAI.MOD_ID, "mpr_integration", "Enhanced AI -> MPR Integration", () -> isEnabled() && ModList.get().isLoaded("mobspropertiesrandomness"));
	}
}
