package insane96mcp.enhancedai.module.mobs.customtargeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = EAIModules.MOBS, description = "Use `config/enhancedai/Custom targeting` to set custom targeting. This might not work properly as the targeting system is not meant to be expanded like this. This is not applied through Data Keys, so can't be changed at runtime.")
public class CustomTargeting extends JsonFeature {

	public static final List<CustomHostileData> CUSTOM_HOSTILE_DEFAULT_LIST = List.of(
			new CustomHostileData(2, ObjTag.of("#enhancedai:config/can_attack_villagers", Registries.ENTITY_TYPE), ObjTag.of("minecraft:villager", Registries.ENTITY_TYPE)).chance(0.5f),
			new CustomHostileData(2, ObjTag.of("#enhancedai:config/can_attack_iron_golem", Registries.ENTITY_TYPE), ObjTag.of("minecraft:iron_golem", Registries.ENTITY_TYPE)).chance(0.5f),
			new CustomHostileData(1, ObjTag.of("#enhancedai:config/hostile_horses", Registries.ENTITY_TYPE), ObjTag.of("minecraft:player", Registries.ENTITY_TYPE)).addAttackGoal()
	);

	public static final List<CustomHostileData> customHostile = new ArrayList<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		this.getJsonConfigs().add(new JsonConfig<>("custom_hostile.json", customHostile, CUSTOM_HOSTILE_DEFAULT_LIST, CustomHostileData.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processCustomTargetGoal(mob);
	}

	private void processCustomTargetGoal(Mob mob) {
		if (customHostile.isEmpty())
			return;
		for (CustomHostileData chd : customHostile) {
			chd.tryApply(mob);
		}
	}
}
