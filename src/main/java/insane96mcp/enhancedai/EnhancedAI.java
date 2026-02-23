package insane96mcp.enhancedai;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import insane96mcp.enhancedai.command.EAICommand;
import insane96mcp.enhancedai.data.PotionEffectList;
import insane96mcp.enhancedai.data.mpr.condition.EAIConditionsRegistry;
import insane96mcp.enhancedai.data.mpr.property.EAIPropertiesRegistry;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.animal.AnimalScaredAttack;
import insane96mcp.enhancedai.modules.mobs.Leaders;
import insane96mcp.enhancedai.modules.mobs.MeleeAttacking;
import insane96mcp.enhancedai.modules.mobs.PushResistance;
import insane96mcp.enhancedai.modules.mobs.miner.MinerMobs;
import insane96mcp.enhancedai.modules.mobs.targeting.Targeting;
import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.setup.ILModConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.DebugPathCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(EnhancedAI.MOD_ID)
public class EnhancedAI
{
    public static final String MOD_ID = "enhancedai";
    public static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    public static final String CONFIG_FOLDER = "config/" + MOD_ID;

    public static ILModConfig CONFIG;

    public EnhancedAI(IEventBus eventBus, ModContainer modContainer) {
        registerConfigTypes();
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, EAIModules::init, EnhancedAI.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);

        NeoForge.EVENT_BUS.register(this);
        //EAISounds.SOUND_EVENTS.register(eventBus);
        EAIAttributes.ATTRIBUTES.register(eventBus);
        //EAIEntities.ENTITIES.register(eventBus);

        eventBus.addListener(MinerMobs::addAttribute);
        eventBus.addListener(AnimalScaredAttack::attribute);
        eventBus.addListener(MeleeAttacking::attributeModificationEvent);
        eventBus.addListener(Targeting::attribute);
        eventBus.addListener(PushResistance::attribute);
        eventBus.addListener(Leaders::attribute);

        if (ModList.get().isLoaded("mobspropertiesrandomness")) {
            EAIPropertiesRegistry.init();
            EAIConditionsRegistry.init();
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

    private static void registerConfigTypes() {
        Feature.registerConfigType(PotionEffectList.class, (builder, name, annotation, defaultValue) ->
                new PotionEffectList.COption(builder, name, annotation.description(), (PotionEffectList) defaultValue));
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
