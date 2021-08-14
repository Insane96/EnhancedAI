package insane96mcp.enhancedai;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.ModAttributes;
import insane96mcp.enhancedai.setup.ModSounds;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnhancedAI.MOD_ID)
public class EnhancedAI
{
	public static final String MOD_ID = "enhancedai";
	public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogManager.getLogger();
    
    public EnhancedAI() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
        
        ModSounds.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		ModAttributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
