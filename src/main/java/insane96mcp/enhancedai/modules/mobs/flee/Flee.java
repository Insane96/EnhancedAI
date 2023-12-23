package insane96mcp.enhancedai.modules.mobs.flee;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Flee", description = "Custom Json config to set mobs from running from other mobs")
@LoadFeature(module = Modules.Ids.MOBS, canBeDisabled = false)
public class Flee extends JsonFeature {
	public static final List<CustomFleeConfig> CUSTOM_FLEE_DEFAULT = List.of();

	public static final List<CustomFleeConfig> customFlee = new ArrayList<>();

	public Flee(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("custom_flee.json", customFlee, CUSTOM_FLEE_DEFAULT, CustomFleeConfig.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof PathfinderMob mob)
				|| customFlee.isEmpty())
			return;

		for (CustomFleeConfig cfc : customFlee) {
			if (!cfc.entity.matchesEntity(mob))
				continue;

			EAAvoidEntityGoal<LivingEntity> avoidEntityGoal = new EAAvoidEntityGoal<>(mob, LivingEntity.class, cfc.fleeFrom, (float) cfc.avoidDistance, (float) cfc.avoidDistanceNear, cfc.speedMultiplier, cfc.speedMultiplierNear);

			mob.targetSelector.addGoal(cfc.priority, avoidEntityGoal);
		}
	}
}
