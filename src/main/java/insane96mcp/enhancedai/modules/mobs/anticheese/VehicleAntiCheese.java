package insane96mcp.enhancedai.modules.mobs.anticheese;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, name = "Anti-Cheese", description = "Allows mobs to break vehicles to escape.")
public class VehicleAntiCheese extends Feature {
    public static final TagKey<EntityType<?>> PREVENT_VEHICLE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/vehicle_anti_cheese/prevent_riding"));
    public static final TagKey<EntityType<?>> CAN_BREAK_VEHICLE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/vehicle_anti_cheese/break_vehicle"));
    public static final TagKey<EntityType<?>> VALID_VEHICLES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/vehicle_anti_cheese/valid_vehicles"));

    @Config(description = "If true, entity types in the enhancedai:vehicle_anti_cheese/prevent_riding tag will not be able to mount vehicles in `enhancedai:mobs/vehicle_anti_cheese/valid_vehicles`.")
    public static Boolean preventRidingVehicles = false;

    @Config(description = "If true, entity types in the enhancedai:mobs/vehicle_anti_cheese/break_vehicle tag will get an AI to break vehicles in `enhancedai:mobs/vehicle_anti_cheese/valid_vehicles` tag.")
    public static Boolean breakVehicles = true;

	public static EAIData<Boolean> PREVENT_RIDING;
	public static EAIData<Boolean> BREAK_VEHICLE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		PREVENT_RIDING = EAIData.ofBool(this.createDataKey("prevent_riding"));
		BREAK_VEHICLE = EAIData.ofBool(this.createDataKey("break_vehicle"), (mob, canBreakVehicle) -> {
			GoalHelper.removeGoal(mob.goalSelector, BreakVehicleGoal.class);
			if (canBreakVehicle)
				mob.goalSelector.addGoal(1, new BreakVehicleGoal(mob));
		});
    }

    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !(event.getEntityBeingMounted().getType().is(VALID_VEHICLES))
                || !PREVENT_RIDING.get(event.getEntity()))
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Mob mob))
            return;

		if (mob.getType().is(CAN_BREAK_VEHICLE))
			BREAK_VEHICLE.applyIfAbsent(mob, breakVehicles);
		if (mob.getType().is(PREVENT_VEHICLE))
			PREVENT_RIDING.applyIfAbsent(mob, preventRidingVehicles);
    }
}