package insane96mcp.enhancedai.module.mobs.breakanger;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = EAIModules.MOBS, description = "Make mobs angry when a block is broken. Check the mod's config folder Mobs/Break Anger/break_anger_config.json to change blocks and entities triggered.")
public class BreakAnger extends JsonFeature {

	public static final List<BreakAngerConfig> ANGERING_LIST_DEFAULT = List.of(
		new BreakAngerConfig(ObjTag.tagOf(ResourceLocation.parse("forge:ores/quartz"), Registries.BLOCK), ObjTag.objOf(ResourceLocation.parse("minecraft:zombified_piglin"), Registries.ENTITY_TYPE), 32d, false)
	);

	public static final List<BreakAngerConfig> angeringList = new ArrayList<>();

	//TODO Per mob?
	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		this.getJsonConfigs().add(new JsonConfig<>("break_anger_config.json", angeringList, ANGERING_LIST_DEFAULT, BreakAngerConfig.LIST_TYPE));
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
			if (!breakAngerConfig.block.matches(event.getState().getBlock()))
				continue;
			entities.stream()
					.filter(mob -> breakAngerConfig.entity.matches(mob.getType()))
					.filter(mob -> !breakAngerConfig.requiresLineOfSight || mob.hasLineOfSight(player))
					.forEach(mob -> mob.setTarget(player));
		}
	}
}
