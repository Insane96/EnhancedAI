package insane96mcp.enhancedai.modules.enderman.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.enderman.ai.GetOverHereGoal;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "[Experimental] Get Over Here", description = "Endermen teleport the player near him when can't reach him for a while.")
@LoadFeature(module = Modules.Ids.ENDERMAN, enabledByDefault = false)
public class GetOverHere extends Feature {

    public static final String GET_OVER_HERE = EnhancedAI.RESOURCE_PREFIX + "get_over_here";
    @Config(min = 0d, max = 1d)
    @Label(name = "Get Over Here Chance", description = "Chance for a enderman to get the Get Over Here AI")
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

        boolean getOverHere = NBTUtils.getBooleanOrPutDefault(enderman.getPersistentData(), GET_OVER_HERE, enderman.getRandom().nextDouble() < getOverHereChance);

        if (!getOverHere)
            return;

        GetOverHereGoal getOverHereGoal = new GetOverHereGoal(enderman);
        enderman.goalSelector.addGoal(1, getOverHereGoal);
    }
}