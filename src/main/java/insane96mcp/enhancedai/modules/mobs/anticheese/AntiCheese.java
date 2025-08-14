package insane96mcp.enhancedai.modules.mobs.anticheese;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, name = "Anti-Cheese", description = "Prevent players from abusing some game mechanics to stop mobs, like vehicles or 2 block tall holes for endermen.")
public class AntiCheese extends Feature {
    public static final TagKey<EntityType<?>> PREVENT_VEHICLE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("anti_cheese/prevent_vehicle"));
    public static final TagKey<EntityType<?>> BREAK_VEHICLE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("anti_cheese/break_vehicle"));
    public static final TagKey<EntityType<?>> VALID_VEHICLES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("anti_cheese/valid_vehicles"));
    public static final TagKey<EntityType<?>> TELEPORT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("anti_cheese/teleport"));
    public static final TagKey<EntityType<?>> CANT_BE_TELEPORTED = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("anti_cheese/cant_be_teleported"));

    @Config(min = 0d, max = 1d, description = "Chance for entity types in the enhancedai:anti_cheese/teleport tag to get the Teleport Anti-Cheese AI, teleporting the target near them after not being able to reach them. Entity types in the tag enhancedai:anti_cheese/cant_be_teleported can't be teleported")
    public static Double teleportAntiCheese = 1d;

    @Config(description = "If true, entity types in the enhancedai:anti_cheese/prevent_vehicle tag will not be able to mount vehicles in `enhancedai:anti_cheese/valid_vehicles`.")
    public static Boolean preventRidingVehicles = false;

    @Config(description = "If true, entity types in the enhancedai:anti_cheese/break_vehicle tag will get an AI to break vehicles in `enhancedai:anti_cheese/valid_vehicles` tag.")
    public static Boolean breakVehicles = true;

	public static EAIData<Boolean> ANTI_CHEESE;
	public static EAIData<Boolean> PREVENT_RIDING;
	public static EAIData<Boolean> BREAK_VEHICLE_DATA;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        ANTI_CHEESE = EAIData.ofBool(this.createDataKey("anti_cheese"), (mob, antiCheese) -> {
			GoalHelper.removeGoal(mob.goalSelector, TeleportAntiCheeseGoal.class);
			if (antiCheese)
				mob.goalSelector.addGoal(1, new TeleportAntiCheeseGoal(mob));
		});
		PREVENT_RIDING = EAIData.ofBool(this.createDataKey("prevent_riding"));
		BREAK_VEHICLE_DATA = EAIData.ofBool(this.createDataKey("break_vehicle"));
    }

    @SubscribeEvent
    public void onMount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !(event.getEntityBeingMounted().getType().is(VALID_VEHICLES))
                || !event.getEntityMounting().getType().is(PREVENT_VEHICLE)
                || !PREVENT_RIDING.get(event.getEntity()))
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
				|| !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(BREAK_VEHICLE)
                || !BREAK_VEHICLE_DATA.get(mob))
            return;

        mob.goalSelector.addGoal(1, new BreakVehicleGoal(mob));
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(TELEPORT))
            return;

		ANTI_CHEESE.applyIfAbsent(mob, mob.getRandom().nextDouble() < teleportAntiCheese);
		PREVENT_RIDING.applyIfAbsent(mob, preventRidingVehicles);
		BREAK_VEHICLE_DATA.applyIfAbsent(mob, breakVehicles);
    }
}