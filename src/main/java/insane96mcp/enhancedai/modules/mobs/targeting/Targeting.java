package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAIHurtByTargetGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LoadFeature(module = Modules.Ids.MOBS, description = "Change how mobs target players. Check the config options below for entity type tags.")
public class Targeting extends JsonFeature {
	public static final TagKey<EntityType<?>> CHANGE_FOLLOW_RANGE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/follow_range_override"));
	public static final TagKey<EntityType<?>> BETTER_HURT_BY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/better_hurt_by"));
	public static final TagKey<EntityType<?>> BETTER_NEARBY_TARGETING = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/better_nearby_targeting"));
	public static final TagKey<EntityType<?>> ALLOW_TARGET_SWITCH = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/allow_target_switch"));
	public static final TagKey<EntityType<?>> APPLY_XRAY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/apply_xray"));
	public static final TagKey<EntityType<?>> VISITED_NODES_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/visited_nodes_multiplier"));
	public static final TagKey<EntityType<?>> ALERT_NEARBY = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/targeting/alert_nearby"));

	@Config(min = 0d, max = 128d, description = "How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting 'Max' to 0 will leave the follow range as vanilla. Use mods like Mobs Properties Randomness to have more control over the attribute. Only mobs in the entity type tag `enhancedai:mobs/targeting/follow_range_override` will be affected by this override")
	public static MinMax followRangeOverride = new MinMax(32, 48);
    @Config(min = 0d, max = 128d, description = "How far away can the mobs see the player even through walls. This only works with 'Better Nearby Targeting' enabled. Setting 'Max' to 0 will make mobs not able to see through walls. Use mods like Mobs Properties Randomness to have more control over the attribute; the attribute name is 'enhancedai:xray_follow_range'. Only mobs in the entity type tag `enhancedai:mobs/targeting/apply_xray` will be affected by this override.")
    public static MinMax xrayRangeOverride = new MinMax(16, 24);
    @Config(min = 0d, max = 1d, description = "Chance for a mob the get the xray range override.")
    public static Double xrayRangeOverrideChance = 0.5d;

    @Config
    public static Boolean seeGlowingEntities = true;

	@Config(description = "Mobs will actually switch target when attacked unless it's the same or if the current one it's closer. Only entity types in the entity type tag `enhancedai:mobs/targeting/better_hurt_by` tag will be affected by this. Use the entity type tag `enhancedai:mobs/targeting/allow_target_switch` to allow more entity types to switch targets (e.g. creepers in vanilla can't switch targets).")
	public static Boolean betterHurtByTarget$enable = true;
	@Config(description = "Setting this to true allows overriding target AI only for players.")
	public static Boolean betterHurtByTarget$playerOnly = false;
	@Config(description = "Mobs will prefer to attack players instead of other mobs (Note that 'Prevent infighting' should be disabled).")
	public static Boolean betterHurtByTarget$preferPlayers = false;
	@Config(min = 0d, max = 1d, description = "Change for a mob to not attack other mobs when hit.")
	public static Double betterHurtByTarget$preventInfighting = 0.9d;

    @Config(min = 0, description = "Mobs in the entity type tag `enhancedai:mobs/targeting/alert_nearby` will alert nearby mobs in this range and target the player.")
    public static Integer alertRange = 32;

	@Config(description = "Mobs NearestAttackableTargetGoal will be replaced with mod's one for better configuration and targeting.")
	public static Boolean betterNearbyTargeting$enable = true;
	@Config(description = "1 in x chance every other tick for a mob to target a nearby entity. Vanilla is 10. Setting to 0 will make the mob instantly target entities. The higher the more time will take mobs to target entities.")
	public static Integer betterHurtByTarget$targetChance = 7;
	@Config(min = 0d, max = 1d, description = "Chances for a mob to spawn neutral (so will not attack players until provoked)")
	public static Difficulty betterNearbyTargeting$neutralChances = new Difficulty(0.25d, 0.10d, 0.04d);

	@Config(description = "Mobs will be able to find better and longer paths to the target the higher this value is. The higher the more performance heavy. Only entity types in the tag `enhancedai:mobs/targeting/visited_nodes_multiplier` tag will be affected by this. Vanilla is 1.0")
	public static Double maxVisitedNodesMultiplier = 4d;

	public static ResourceLocation FOLLOW_RANGES_PROCESSED;
	public static ResourceLocation NEUTRAL;
	public static EAIData<Double> MAX_VISITED_NODES_MULTIPLIER;
	public static EAIData<Boolean> HURT_BY_PREFER_PLAYERS;
	public static EAIData<Boolean> HURT_BY_PREVENT_INFIGHTING;
	public static EAIData<Integer> TARGET_CHANCE;
	public static EAIData<Integer> UNSEEN_FORGET_TICKS;
	public static EAIData<Integer> ALERT_RANGE;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		FOLLOW_RANGES_PROCESSED = this.createDataKey("follow_ranges_processed");
		NEUTRAL = this.createDataKey("neutral");
		MAX_VISITED_NODES_MULTIPLIER = EAIData.ofDouble(this.createDataKey("max_visited_nodes_multiplier"),
				(mob, multiplier) -> mob.getNavigation().setMaxVisitedNodesMultiplier(multiplier.floatValue()));
		HURT_BY_PREFER_PLAYERS = EAIData.ofBool(this.createDataKey("hurt_by_prefer_players"));
		HURT_BY_PREVENT_INFIGHTING = EAIData.ofBool(this.createDataKey("hurt_by_prevent_infighting"), (mob, preventInfighting) ->
			GoalHelper.getGoal(mob.targetSelector, EAIHurtByTargetGoal.class).ifPresent(goal -> {
				if (preventInfighting) goal.preventInfighting();
				else goal.allowInfighting();
			})
		);
		TARGET_CHANCE = EAIData.ofInt(this.createDataKey("target_chance"));
		UNSEEN_FORGET_TICKS = EAIData.ofInt(this.createDataKey("unseen_forget_ticks"));
        ALERT_RANGE = EAIData.ofInt(this.createDataKey("alert_range"));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	public static void attribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, EAIAttributes.XRAY_FOLLOW_RANGE.get()))
				event.add(entityType, EAIAttributes.XRAY_FOLLOW_RANGE.get(), 0d);
		}
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processFollowRanges(mob);
		processTargetGoal(mob);
		processHurtByGoal(mob);
		processMaxTargetingNodes(mob);
        ALERT_RANGE.applyIfAbsent(mob, alertRange);
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
					&& mob.getAttribute(EAIAttributes.XRAY_FOLLOW_RANGE.get()) != null
                    && mob.getRandom().nextFloat() < xrayRangeOverrideChance) {
				MCUtils.setAttributeValue(mob, EAIAttributes.XRAY_FOLLOW_RANGE.get(), xrayRangeOverride.getIntRandBetween(mob.getRandom()));
			}
		}
		ModNBTData.put(mob, FOLLOW_RANGES_PROCESSED, true);
	}

	private void processTargetGoal(Mob mob) {
		if (!betterNearbyTargeting$enable
				|| !mob.getType().is(BETTER_NEARBY_TARGETING))
			return;

		List<NearestAttackableTargetGoal<?>> toRemove = new ArrayList<>();
		List<WrappedGoal> toAdd = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> goal))
				continue;

			if (goal.targetType != Player.class && betterHurtByTarget$playerOnly)
				continue;

			toRemove.add(goal);

			boolean neutral = NBTUtils.getBooleanOrPutDefault(mob, NEUTRAL, mob.getRandom().nextDouble() < betterNearbyTargeting$neutralChances.getByDifficulty(mob.level()));
			if (neutral && goal.targetType == Player.class)
				continue;

            EAINearestAttackableTarget<? extends LivingEntity> newTargetGoal = createTargetGoal(mob, goal);

            toAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newTargetGoal));
		}
		toRemove.forEach(mob.targetSelector::removeGoal);
		toAdd.forEach(wrappedGoal ->
				mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));
		TARGET_CHANCE.applyIfAbsent(mob, betterHurtByTarget$targetChance);
	}

    private static EAINearestAttackableTarget<? extends LivingEntity> createTargetGoal(Mob mob, NearestAttackableTargetGoal<?> goal) {
        EAINearestAttackableTarget<? extends LivingEntity> newTargetGoal = new EAINearestAttackableTarget<>(mob, goal.targetType, false, true, goal.targetConditions);;

        if (mob instanceof Spider spider)
            newTargetGoal = new EAISpiderTargetGoal<>(spider, goal.targetType, false, true, goal.targetConditions);
        else if (mob instanceof Shulker shulker) {
            if (goal instanceof Shulker.ShulkerNearestAttackGoal)
                newTargetGoal = new EAIShulkerNearestAttackTargetGoal<>(shulker, goal.targetType, false, true, goal.targetConditions);
            else if (goal instanceof Shulker.ShulkerDefenseAttackGoal)
                newTargetGoal = new EAIShulkerNearestDefenseTargetGoal<>(shulker, goal.targetType, false, true, goal.targetConditions);
        }

        return newTargetGoal;
    }

    private void processHurtByGoal(Mob mob) {
		if (!betterHurtByTarget$enable
				|| !mob.getType().is(BETTER_HURT_BY))
			return;

		List<HurtByTargetGoal> toRemove = new ArrayList<>();
		List<WrappedGoal> toAdd = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof HurtByTargetGoal goal))
				continue;
			toRemove.add(goal);

			List<Class<?>> toIgnoreDamage = new ArrayList<>(Arrays.asList(goal.toIgnoreDamage));
			EAIHurtByTargetGoal newGoal = new EAIHurtByTargetGoal(mob, toIgnoreDamage.toArray(Class[]::new));
			if (goal.toIgnoreAlert != null)
				newGoal.setAlertOthers(goal.toIgnoreAlert);
			toAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newGoal));
		}

		toAdd.forEach(wrappedGoal -> mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));
		if (!toRemove.isEmpty())
			toRemove.forEach(mob.targetSelector::removeGoal);
		//If I can't find a hurt by goal, add one to mobs that are now allowed switch target
		else if (mob.getType().is(ALLOW_TARGET_SWITCH)) {
			EAIHurtByTargetGoal newGoal = new EAIHurtByTargetGoal(mob);
			mob.targetSelector.addGoal(1, newGoal);
		}

		HURT_BY_PREFER_PLAYERS.applyIfAbsent(mob, betterHurtByTarget$preferPlayers);
		HURT_BY_PREVENT_INFIGHTING.applyIfAbsent(mob, mob.getRandom().nextFloat() < betterHurtByTarget$preventInfighting);
	}

	private void processMaxTargetingNodes(Mob mob) {
		if (!mob.getType().is(VISITED_NODES_MULTIPLIER))
			return;
		MAX_VISITED_NODES_MULTIPLIER.applyIfAbsent(mob, maxVisitedNodesMultiplier);
	}

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (!this.isEnabled()
                || alertRange <= 0
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(ALERT_NEARBY)
                || !(event.getSource().getEntity() instanceof ServerPlayer attacker)
                || !attacker.gameMode.isSurvival())
            return;

        event.getEntity().level()
                .getEntities(mob, mob.getBoundingBox().inflate(ALERT_RANGE.get(mob)), entity -> entity.getType() == mob.getType())
                .forEach(entity -> {
                    if (!(entity instanceof Mob nearbyMob))
                        return;
                    //Don't switch target if current one is closer
                    if (nearbyMob.getTarget() != null && nearbyMob.distanceToSqr(nearbyMob.getTarget()) <= nearbyMob.distanceToSqr(attacker))
                        return;
                    nearbyMob.setTarget(attacker);
                });
    }
}
