package insane96mcp.enhancedai.module.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIDataBlockPos;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = EAIModules.ANIMAL, description = "Tamed horses, donkeys, mules and similar don't wander too far away from where the player dismounted them. Only entity types in `enhancedai:animal/stay_near_dismount` tag are affected by this feature.")
public class StayNearDismount extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.id("animal/stay_near_dismount"));

	@Config(min = 1, max = 64, description = "Max distance a horse wanders away from where the player dismounted it.")
	public static Integer range = 10;

	public static EAIDataBlockPos DISMOUNT_POS;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		DISMOUNT_POS = EAIDataBlockPos.of(this.createDataKey("dismount_pos"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof AbstractHorse horse)
				|| !horse.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		//Only runs when the horse is restricted (and not ridden), makes it go back if it ends up out of range (e.g. pushed away)
		GoalHelper.removeGoal(horse.goalSelector, MoveTowardsRestrictionGoal.class);
		horse.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(horse, 1.0d));
	}

	@SubscribeEvent
	public void onMount(EntityMountEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntityBeingMounted() instanceof AbstractHorse horse)
				|| !horse.getType().is(AFFECTED_ENTITY_TYPES)
				|| !(event.getEntityMounting() instanceof Player))
			return;

		if (event.isMounting()) {
			if (!DISMOUNT_POS.has(horse))
				return;
			ModNBTData.remove(horse, DISMOUNT_POS.id());
			if (!horse.isLeashed())
				horse.clearRestriction();
		}
		else if (horse.isTamed() && horse.isAlive()) {
			BlockPos pos = horse.blockPosition();
			DISMOUNT_POS.apply(horse, pos);
			if (!horse.isLeashed())
				horse.restrictTo(pos, range);
		}
	}

	@SubscribeEvent
	public void onHorseTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof AbstractHorse horse)
				|| horse.level().isClientSide
				|| horse.tickCount % 20 != 0
				|| !DISMOUNT_POS.has(horse))
			return;

		//While leashed vanilla restricts the horse around the leash holder, so the anchor follows the horse and becomes where it gets unleashed
		if (horse.isLeashed())
			DISMOUNT_POS.apply(horse, horse.blockPosition());
		//The restriction is not saved and is cleared when the leash is removed, so it's re-applied
		else if (!horse.hasRestriction())
			horse.restrictTo(DISMOUNT_POS.get(horse), range);
	}
}
