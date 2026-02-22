package insane96mcp.enhancedai.modules.spider;

import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.world.entity.monster.Spider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.Ids.SPIDER)
public class StuckFix extends Feature {
	@Config(description = "Adds a new AI to spiders that will prevent them from climbing if they were stuck on a wall for more than 2 seconds and will detach them launching towards the target.")
	public static Boolean stuckFix = true;

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Spider spider)
				|| !stuckFix)
			return;
		spider.goalSelector.addGoal(0, new DestuckWallGoal(spider));
	}
}