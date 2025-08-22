package insane96mcp.enhancedai.modules.spider;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SPIDER)
public class StuckFix extends Feature {
	@Config(description = "Adds a new AI to spiders that will prevent them from climbing if they were stuck on a wall for more than 2 seconds.")
	public static Boolean stuckFix = true;

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Spider spider)
				|| !stuckFix)
			return;
		spider.goalSelector.addGoal(0, new DestuckWallGoal(spider));
	}
}