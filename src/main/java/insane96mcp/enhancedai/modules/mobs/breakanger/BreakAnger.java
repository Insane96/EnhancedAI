package insane96mcp.enhancedai.modules.mobs.breakanger;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = Modules.Ids.MOBS, description = "Make mobs angry when a block is broken. Check the mod's config folder Mobs/Break Anger/break_anger_config.json to change blocks and entities triggered.")
public class BreakAnger extends JsonFeature {

	public static final List<BreakAngerConfig> ANGERING_LIST_DEFAULT = List.of(
		new BreakAngerConfig(IdTagMatcher.newTag("forge:ores/quartz"), IdTagMatcher.newId("minecraft:zombified_piglin"), 32d, false)
	);

	public static final List<BreakAngerConfig> angeringList = new ArrayList<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
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
			List<Mob> entities = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(breakAngerConfig.range));
			if (!breakAngerConfig.block.matchesBlock(event.getState()))
				continue;
			entities.stream()
					.filter(mob -> breakAngerConfig.entity.matchesEntity(mob))
					.filter(mob -> !breakAngerConfig.requiresLineOfSight || mob.hasLineOfSight(player))
					.forEach(mob -> mob.setTarget(player));
		}
	}
}
