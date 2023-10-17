package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Piglins Quartz", description = "Zombiefied Piglins get angry at players mining Quartz")
@LoadFeature(module = Modules.Ids.MOBS)
public class PiglinsQuartz extends Feature {
	@Config
	@Label(name = "Entity Blacklist", description = "Entities in this list will not be affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public PiglinsQuartz(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onBlockDestroyed(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| !(event.getPlayer() instanceof ServerPlayer player)
				|| !event.getState().is(Tags.Blocks.ORES_QUARTZ))
			return;

		player.level().getEntitiesOfClass(ZombifiedPiglin.class, player.getBoundingBox().inflate(32d)).forEach(zombifiedPiglin -> zombifiedPiglin.setTarget(player));
	}
}
