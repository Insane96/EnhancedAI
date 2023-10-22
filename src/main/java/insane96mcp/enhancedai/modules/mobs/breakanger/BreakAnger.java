package insane96mcp.enhancedai.modules.mobs.breakanger;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Break Anger", description = "Make mobs angry when a block is broken. Check the mod's config folder Mobs/Break Anger/break_anger_config.json to change blocks and entities triggered.")
@LoadFeature(module = Modules.Ids.MOBS)
public class BreakAnger extends JsonFeature {

	public static final List<BreakAngerConfig> ANGERING_LIST_DEFAULT = List.of(
		new BreakAngerConfig(IdTagMatcher.newTag("forge:ores/quartz"), IdTagMatcher.newId("minecraft:zombified_piglin"), 32d)
	);

	public static final List<BreakAngerConfig> angeringList = new ArrayList<>();

	public BreakAnger(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("break_anger_config.json", angeringList, ANGERING_LIST_DEFAULT, BreakAngerConfig.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onBlockDestroyed(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| !(event.getPlayer() instanceof ServerPlayer player))
			return;

		for (BreakAngerConfig breakAngerConfig : angeringList) {
			if (breakAngerConfig.block.matchesBlock(event.getState())) {
				player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(breakAngerConfig.range), mob -> breakAngerConfig.entity.matchesEntity(mob))
						.forEach(mob -> mob.setTarget(player));
			}
		}
	}
}
