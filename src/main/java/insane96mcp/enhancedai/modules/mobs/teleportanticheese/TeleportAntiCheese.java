package insane96mcp.enhancedai.modules.mobs.teleportanticheese;

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
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, name = "Teleport anti-cheese", description = "Prevent players from abusing water or 2 high gaps to bully endermen.")
public class TeleportAntiCheese extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/teleport_anti_cheese/can_use"));
    public static final TagKey<EntityType<?>> CANT_BE_TELEPORTED = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/teleport_anti_cheese/cant_be_teleported"));

    @Config(min = 0d, max = 1d, description = "Chance for entity types in the enhancedai:anti_cheese/teleport tag to get the Teleport Anti-Cheese AI, teleporting the target near them after not being able to reach them. Entity types in the tag enhancedai:anti_cheese/cant_be_teleported can't be teleported")
    public static Double chance = 1d;

	public static EAIData<Boolean> DATA;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        DATA = EAIData.ofBool(this.createDataKey("use_teleport_anti_cheese"), (mob, antiCheese) -> {
			GoalHelper.removeGoal(mob.goalSelector, TeleportAntiCheeseGoal.class);
			if (antiCheese)
				mob.goalSelector.addGoal(1, new TeleportAntiCheeseGoal(mob));
		});
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		DATA.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
    }
}