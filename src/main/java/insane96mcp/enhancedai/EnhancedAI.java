package insane96mcp.enhancedai;

import com.mojang.brigadier.CommandDispatcher;
import insane96mcp.enhancedai.command.EAICommand;
import insane96mcp.enhancedai.data.mpr.EAIChangeDataProperty;
import insane96mcp.enhancedai.modules.animal.AnimalScaredAttack;
import insane96mcp.enhancedai.modules.mobs.MeleeAttacking;
import insane96mcp.enhancedai.modules.mobs.miner.MinerMobs;
import insane96mcp.enhancedai.modules.mobs.targeting.Targeting;
import insane96mcp.enhancedai.setup.*;
import insane96mcp.mobspropertiesrandomness.data.json.property.PropertiesRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.DebugPathCommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EnhancedAI.MOD_ID)
public class EnhancedAI
{
	public static final String MOD_ID = "enhancedai";
    @Deprecated
    /// Use EnhancedAI.location instead
	public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String CONFIG_FOLDER = "config/" + MOD_ID;
    
    public EnhancedAI(FMLJavaModLoadingContext context) {
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, EAIConfig.COMMON_SPEC, MOD_ID + "/common.toml");

        MinecraftForge.EVENT_BUS.register(this);
		IEventBus modEventBus = context.getModEventBus();
		EAISounds.SOUND_EVENTS.register(modEventBus);
		EAIAttributes.ATTRIBUTES.register(modEventBus);
		EAIEntities.ENTITIES.register(modEventBus);

        Reflection.init();

		modEventBus.addListener(MinerMobs::addAttribute);
        modEventBus.addListener(AnimalScaredAttack::attribute);
        modEventBus.addListener(MeleeAttacking::attributeModificationEvent);
        modEventBus.addListener(Targeting::xrayRangeAttribute);

        if (ModList.get().isLoaded("mobspropertiesrandomness")) {
            PropertiesRegistry.PROPERTIES.put(location("change_data"), EAIChangeDataProperty.class);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext context = event.getBuildContext();
        EAICommand.register(dispatcher, context);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DebugPathCommand.register(event.getDispatcher());
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
