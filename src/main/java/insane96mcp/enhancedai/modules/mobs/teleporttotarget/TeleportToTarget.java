package insane96mcp.enhancedai.modules.mobs.teleporttotarget;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes mobs teleport other mobs close to the target. Mobs in the `enhancedai:mobs/teleport_to_target/can_be_teleported` tag will be able to be picked up, while mobs in the `enhancedai:mobs/teleport_to_target/can_teleport` tag will be able to pick up other mobs.")
public class TeleportToTarget extends Feature {
    public static final TagKey<EntityType<?>> CAN_BE_TELEPORTED = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/teleport_to_target/can_be_teleported"));
    public static final TagKey<EntityType<?>> CAN_TELEPORT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/teleport_to_target/can_teleport"));

    @Config(min = 0d, max = 1d, description = "Chance for a mob to have an AI to go and pick up and throw mobs.")
    public static Difficulty chance = new Difficulty(0.35d, 0.35d, 0.5d);
    @Config(min = 0, description = "Cooldown (in ticks) after throwing a mob. Also goes on cooldown if can't reach the targeted mob for a few seconds")
    public static Integer cooldown = 600;

	public static EAIData<String> CAN_TELEPORT_DATA;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		CAN_TELEPORT_DATA = EAIData.ofString(this.createDataKey("can_teleport"));
	}

	@SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_TELEPORT)
                || mob.isBaby())
            return;

		if (mob.getRandom().nextDouble() < chance.getByDifficulty(mob.level()))
            CAN_TELEPORT_DATA.applyIfAbsent(mob, CAN_BE_TELEPORTED.location().toString());
        else
            CAN_TELEPORT_DATA.applyIfAbsent(mob, "");
		mob.targetSelector.addGoal(0, new TeleportToTargetGoal(mob));
    }
}
