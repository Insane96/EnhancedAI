package insane96mcp.enhancedai.modules.enderman.getoverhere;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(
        module = Modules.Ids.ENDERMAN,
        enabledByDefault = false,
        name = "[Experimental] Get Over Here",
        description = "Endermen teleport the player near him when can't reach him for a while."
)
public class GetOverHere extends Feature {
    public static final TagKey<EntityType<?>> TELEPORT_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(EnhancedAI.MOD_ID, "get_over_here_teleport_blacklist"));

    public static final ResourceLocation GET_OVER_HERE = EnhancedAI.location("get_over_here");
    @Config(min = 0d, max = 1d, description = "Chance for a enderman to get the Get Over Here AI")
    public static Double getOverHereChance = 0.15d;

    public GetOverHere(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof EnderMan enderman))
            return;

        boolean getOverHere = NBTUtils.getBooleanOrPutDefaultLegacy(enderman.getPersistentData(), GET_OVER_HERE.toString(), enderman.getRandom().nextDouble() < getOverHereChance);

        if (!getOverHere)
            return;

        GetOverHereGoal getOverHereGoal = new GetOverHereGoal(enderman);
        enderman.goalSelector.addGoal(1, getOverHereGoal);
    }
}