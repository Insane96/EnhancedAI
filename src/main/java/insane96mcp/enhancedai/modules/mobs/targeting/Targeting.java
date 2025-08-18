package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAIHurtByTargetGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@LoadFeature(module = Modules.Ids.MOBS, description = "Change how mobs target players. Check the config options below for entity type tags.")
public class Targeting extends JsonFeature {
	public static final TagKey<EntityType<?>> CHANGE_FOLLOW_RANGE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/follow_range_override"));
	public static final TagKey<EntityType<?>> BETTER_HURT_BY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/better_hurt_by"));
	public static final TagKey<EntityType<?>> ALLOW_TARGET_SWITCH = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/allow_target_switch"));
	public static final TagKey<EntityType<?>> APPLY_XRAY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/apply_xray"));
	public static final TagKey<EntityType<?>> VISITED_NODES_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/visited_nodes_multiplier"));
	public static final TagKey<EntityType<?>> BLINDNESS_RANGE_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("targeting/blindness_range_multiplier"));

	public static final UUID BLINDNESS_FOLLOW_RANGE_UUID = UUID.fromString("b90bdf38-1c2a-4cb2-a85e-8214618e6cf0");

	@Config(min = 0d, max = 128d, description = "How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting 'Max' to 0 will leave the follow range as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute. Only mobs in the entity type tag `enhancedai:targeting/follow_range_override` will be affected by this override")
	public static MinMax followRangeOverride = new MinMax(32, 48);
	@Config(min = 0d, max = 128d, description = "How far away can the mobs see the player even through walls. Setting 'Max' to 0 will make mobs not able to see through walls. I recommend using mods like Mobs Properties Randomness to have more control over the attribute; the attribute name is 'enhancedai:generic.xray_follow_range'. Only mobs in the entity type tag `enhancedai:targeting/apply_xray` will be affected by this override.")
	public static MinMax xrayRangeOverride = new MinMax(16, 24);

	@Config(description = "Mobs will actually switch target when attacked unless it's the same or if the current one it's closer. Only entity types in the entity type tag `enhancedai:targeting/better_hurt_by` tag will be affected by this. Use the entity type tag `enhancedai:targeting/allow_target_switch` to allow more entity types to switch targets (e.g. creepers in vanilla can't switch targets).")
	public static Boolean betterHurtByTarget$enable = true;
	@Config(description = "Mobs will prefer to attack players instead of other mobs (Note that 'Prevent infighting' should be disabled).")
	public static Boolean betterHurtByTarget$preferPlayers = false;
	@Config(min = 0d, max = 1d, description = "Change for a mob to not attack other mobs when hit.")
	public static Double betterHurtByTarget$preventInfighting = 0.9d;

	@Config(description = "Mobs will be able to find better and longer paths to the target the higher this value is. The higher the more performance heavy. Only entity types in the tag `enhancedai:targeting/visited_nodes_multiplier` tag will be affected by this. Vanilla is 1.0")
	public static Double maxVisitedNodesMultiplier = 4d;
	@Config(min = 0d, max = 1d, description = "If the mobs' affected by blindness effect, it's follow range is multiplied by this value.")
	public static Double blindnessRangeMultiplier = .15d;

	public static ResourceLocation FOLLOW_RANGES_PROCESSED;
	public static EAIData<Double> MAX_VISITED_NODES_MULTIPLIER;
	public static EAIData<Double> BLINDNESS_RANGE_MULTIPLIER_DATA;
	public static EAIData<Boolean> HURT_BY_PREFER_PLAYERS;
	public static EAIData<Boolean> HURT_BY_PREVENT_INFIGHTING;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		FOLLOW_RANGES_PROCESSED = this.createDataKey("follow_ranges_processed");
		MAX_VISITED_NODES_MULTIPLIER = EAIData.ofDouble(this.createDataKey("max_visited_nodes_multiplier"),
				(mob, multiplier) -> mob.getNavigation().setMaxVisitedNodesMultiplier(multiplier.floatValue()));
		BLINDNESS_RANGE_MULTIPLIER_DATA = EAIData.ofDouble(this.createDataKey("blindness_range_multiplier"));
		HURT_BY_PREFER_PLAYERS = EAIData.ofBool(this.createDataKey("hurt_by_prefer_players"));
		HURT_BY_PREVENT_INFIGHTING = EAIData.ofBool(this.createDataKey("hurt_by_prevent_infighting"), (mob, preventInfighting) -> {
			GoalHelper.getGoal(mob.goalSelector, EAIHurtByTargetGoal.class).ifPresent(goal -> {
				if (preventInfighting) goal.preventInfighting();
				else goal.allowInfighting();
			});
		});
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	public static void xrayRangeAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, EAIAttributes.XRAY_FOLLOW_RANGE.get()))
				continue;

			event.add(entityType, EAIAttributes.XRAY_FOLLOW_RANGE.get(), 0d);
		}
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processFollowRanges(mob);
		processHurtByGoal(mob);
		processMaxTargetingNodes(mob);
		processBlindnessRangeMultiplier(mob);
	}

	private void processFollowRanges(Mob mob) {
		if (!ModNBTData.get(mob, FOLLOW_RANGES_PROCESSED, Boolean.class)) {
			//noinspection ConstantConditions
			if (mob.getType().is(CHANGE_FOLLOW_RANGE)
					&& followRangeOverride.max != 0d
					&& mob.getAttribute(Attributes.FOLLOW_RANGE) != null) {
				MCUtils.setAttributeValue(mob, Attributes.FOLLOW_RANGE, followRangeOverride.getIntRandBetween(mob.getRandom()));
			}

			//noinspection ConstantConditions
			if (mob.getType().is(APPLY_XRAY)
					&& mob.getAttribute(EAIAttributes.XRAY_FOLLOW_RANGE.get()) != null) {
				MCUtils.setAttributeValue(mob, EAIAttributes.XRAY_FOLLOW_RANGE.get(), xrayRangeOverride.getIntRandBetween(mob.getRandom()));
			}
		}
		ModNBTData.put(mob, FOLLOW_RANGES_PROCESSED, true);
	}

	private void processHurtByGoal(Mob mob) {
		if (!betterHurtByTarget$enable
				|| !(mob instanceof PathfinderMob pathfinderMob)
				|| !pathfinderMob.getType().is(BETTER_HURT_BY))
			return;

		List<HurtByTargetGoal> toRemove = new ArrayList<>();
		List<WrappedGoal> toAdd = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : pathfinderMob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof HurtByTargetGoal goal))
				continue;
			toRemove.add(goal);

			List<Class<?>> toIgnoreDamage = new ArrayList<>(Arrays.asList(goal.toIgnoreDamage));
			EAIHurtByTargetGoal newGoal = new EAIHurtByTargetGoal(pathfinderMob, toIgnoreDamage.toArray(Class[]::new));
			if (goal.toIgnoreAlert != null)
				newGoal.setAlertOthers(goal.toIgnoreAlert);
			toAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newGoal));
		}

		toAdd.forEach(wrappedGoal -> pathfinderMob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));
		if (!toRemove.isEmpty())
			toRemove.forEach(pathfinderMob.targetSelector::removeGoal);
		//If I can't find a hurt by goal, add one to mobs that are now allowed switch target
		else if (mob.getType().is(ALLOW_TARGET_SWITCH)) {
			EAIHurtByTargetGoal newGoal = new EAIHurtByTargetGoal(pathfinderMob);
			pathfinderMob.targetSelector.addGoal(1, newGoal);
		}

		HURT_BY_PREFER_PLAYERS.applyIfAbsent(mob, betterHurtByTarget$preferPlayers);
		HURT_BY_PREVENT_INFIGHTING.applyIfAbsent(mob, mob.getRandom().nextFloat() < betterHurtByTarget$preventInfighting);
	}

	private void processMaxTargetingNodes(Mob mob) {
		if (!mob.getType().is(VISITED_NODES_MULTIPLIER))
			return;
		MAX_VISITED_NODES_MULTIPLIER.applyIfAbsent(mob, maxVisitedNodesMultiplier);
	}

	private void processBlindnessRangeMultiplier(Mob mob) {
		if (!mob.getType().is(BLINDNESS_RANGE_MULTIPLIER))
			return;
		BLINDNESS_RANGE_MULTIPLIER_DATA.applyIfAbsent(mob, blindnessRangeMultiplier);
	}

	@SubscribeEvent
	public void onTargetDistanceMultiplier(LivingEvent.LivingVisibilityEvent event) {
		if (!this.isEnabled()
				|| !BLINDNESS_RANGE_MULTIPLIER_DATA.has(event.getLookingEntity())
				|| !(event.getLookingEntity() instanceof LivingEntity livingEntity)
				|| !livingEntity.hasEffect(MobEffects.BLINDNESS))
			return;

		event.modifyVisibility(BLINDNESS_RANGE_MULTIPLIER_DATA.get(livingEntity));
	}

	@SubscribeEvent
	public void onBlindnessApply(MobEffectEvent.Added event) {
		if (!this.isEnabled()
				|| !BLINDNESS_RANGE_MULTIPLIER_DATA.has(event.getEntity()))
			return;

		MCUtils.applyModifier(event.getEntity(), Attributes.FOLLOW_RANGE, BLINDNESS_FOLLOW_RANGE_UUID, "Enhanced AI Blindness Multiplier", BLINDNESS_RANGE_MULTIPLIER_DATA.get(event.getEntity()) - 1f, AttributeModifier.Operation.MULTIPLY_TOTAL, true);
	}

	@SubscribeEvent
	public void onBlindnessRemove(MobEffectEvent.Remove event) {
		if (!this.isEnabled()
				|| event.getEntity().getAttribute(Attributes.FOLLOW_RANGE) == null)
			return;

		event.getEntity().getAttribute(Attributes.FOLLOW_RANGE).removeModifier(BLINDNESS_FOLLOW_RANGE_UUID);
	}
}
