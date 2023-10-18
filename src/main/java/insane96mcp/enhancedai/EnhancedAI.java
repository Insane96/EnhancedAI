package insane96mcp.enhancedai;

import insane96mcp.enhancedai.modules.animal.AnimalScaredAttack;
import insane96mcp.enhancedai.modules.base.Attacking;
import insane96mcp.enhancedai.modules.base.targeting.Targeting;
import insane96mcp.enhancedai.setup.*;
import net.minecraft.server.commands.DebugPathCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    public static final String CONFIG_FOLDER = "config/" + MOD_ID;
    
    public EnhancedAI() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);

        MinecraftForge.EVENT_BUS.register(this);

        EASounds.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		EAAttributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
		EAEntities.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        Reflection.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(AnimalScaredAttack::attribute);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Attacking::attributeModificationEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Targeting::xrayRangeAttribute);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DebugPathCommand.register(event.getDispatcher());
    }
}
