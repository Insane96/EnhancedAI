package insane96mcp.enhancedai.modules.mobs.flee;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.JsonFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

//@LoadFeature(module = Modules.Ids.MOBS, canBeDisabled = false, description = "Custom Json config to set mobs from running from other mobs")
public class Flee extends JsonFeature {
	public static final List<CustomFleeConfig> CUSTOM_FLEE_DEFAULT = List.of(new CustomFleeConfig(1, IdTagMatcher.newTag("enhancedai:all_mobs"), IdTagMatcher.newId("minecraft:warden"), 1d, 16, 8, 1, 1.1));

	public static final List<CustomFleeConfig> customFlee = new ArrayList<>();

    public static ResourceLocation CUSTOM_FLEE;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("custom_flee.json", customFlee, CUSTOM_FLEE_DEFAULT, CustomFleeConfig.LIST_TYPE));

        CUSTOM_FLEE = this.createDataKey("custom_flee");
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof PathfinderMob mob)
				|| customFlee.isEmpty())
			return;

        if (!ModNBTData.contains(mob, CUSTOM_FLEE)) {
            for (CustomFleeConfig cfc : customFlee) {
                if (!cfc.entity.matchesEntity(mob) || mob.getRandom().nextFloat() > cfc.chance)
                    continue;

                cfc.tryApply(mob);
            }
        }

        //TODO add /remove goal
	}
}
