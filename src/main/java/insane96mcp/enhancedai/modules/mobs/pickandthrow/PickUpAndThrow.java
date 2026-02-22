package insane96mcp.enhancedai.modules.mobs.pickandthrow;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.Difficulty;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Makes mobs pick up and throw other mobs. Mobs in the `enhancedai:mobs/pick_up_and_throw/can_be_picked_up` tag will be able to be picked up, while mobs in the `enhancedai:mobs/pick_up_and_throw/can_pick_up` tag will be able to pick up other mobs.")
public class PickUpAndThrow extends Feature {
    public static final TagKey<EntityType<?>> CAN_BE_PICKED_UP = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/pick_up_and_throw/can_be_picked_up"));
    public static final TagKey<EntityType<?>> CAN_PICK_UP = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/pick_up_and_throw/can_pick_up"));

    @Config(min = 0d, max = 1d, description = "Chance for a mob to have an AI to go and pick up and throw mobs.")
    public static Difficulty chance = new Difficulty(0.05d, 0.05d, 0.1d);
    @Config(min = 0d, description = "Min distance from the target to let the mob pick up a mob")
    public static Integer minDistanceToPickUp = 5;
    @Config(min = 0, description = "Max distance beyond which the mob will not throw the picked up mob")
    public static Integer maxDistanceToThrow = 24;
    @Config(min = 0d, description = "Speed modifier to apply to the mob when it picks up another mob")
    public static Double speedModifierToPickUp = 1.25d;
    @Config(min = 0, description = "Cooldown (in ticks) after throwing a mob. Also goes on cooldown if can't reach the targeted mob for a few seconds")
    public static Integer cooldown = 600;

	public static EAIData<String> CAN_PICK_UP_DATA;
	public static EAIData<Integer> MIN_DISTANCE_TO_PICK_UP;
	public static EAIData<Integer> MAX_DISTANCE_TO_THROW;
	public static EAIData<Double> SPEED_MODIFIER_TO_PICK_UP;
	public static EAIData<Integer> COOLDOWN;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		CAN_PICK_UP_DATA = EAIData.ofString(this.createDataKey("can_pick_up"));
        MIN_DISTANCE_TO_PICK_UP = EAIData.ofInt(this.createDataKey("min_distance_to_pick_up"));
        MAX_DISTANCE_TO_THROW = EAIData.ofInt(this.createDataKey("max_distance_to_throw"));
        SPEED_MODIFIER_TO_PICK_UP = EAIData.ofDouble(this.createDataKey("speed_modifier_to_pick_up"));
        COOLDOWN = EAIData.ofInt(this.createDataKey("cooldown"));
	}

	@SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_PICK_UP)
                || mob.isBaby())
            return;

		if (mob.getRandom().nextDouble() < chance.getByDifficulty(mob.level()))
			CAN_PICK_UP_DATA.applyIfAbsent(mob, CAN_BE_PICKED_UP.location().toString());
        else
            CAN_PICK_UP_DATA.applyIfAbsent(mob, "");
        MIN_DISTANCE_TO_PICK_UP.applyIfAbsent(mob, minDistanceToPickUp);
        MAX_DISTANCE_TO_THROW.applyIfAbsent(mob, maxDistanceToThrow);
        SPEED_MODIFIER_TO_PICK_UP.applyIfAbsent(mob, speedModifierToPickUp);
        COOLDOWN.applyIfAbsent(mob, cooldown);
		mob.targetSelector.addGoal(0, new PickUpAndThrowGoal(mob));
    }
}
