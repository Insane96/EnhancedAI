package insane96mcp.enhancedai.modules.mobs.targeting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAHurtByTargetGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Targeting", description = "Change how mobs target players. Use the enhancedai:no_target_changes and enhancedai:no_follow_range_changes entity type tag to blacklist mobs. Add mobs to enhancedai:allow_target_change entity type tag to allow these mobs to be able to switch targets when hit (e.g. Creepers can't normally do that).")
@LoadFeature(module = Modules.Ids.MOBS)
public class Targeting extends Feature {
	public static final TagKey<EntityType<?>> NO_TARGET_CHANGES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "no_target_changes"));
	public static final TagKey<EntityType<?>> NO_FOLLOW_RANGE_CHANGES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "no_follow_range_changes"));
	public static final TagKey<EntityType<?>> ALLOW_TARGET_CHANGE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "allow_target_change"));

	public static final String IS_NEUTRAL = EnhancedAI.RESOURCE_PREFIX + "is_neutral";
    public static final String FOLLOW_RANGES_PROCESSED = EnhancedAI.RESOURCE_PREFIX + "follow_ranges_processed";

    @Config(min = 0d, max = 128d)
	@Label(name = "Follow Range Override", description = "How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting 'Max' to 0 will leave the follow range as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
	public static MinMax followRangeOverride = new MinMax(24, 48);
	@Config(min = 0d, max = 128d)
	@Label(name = "XRay Range Override", description = "How far away can the mobs see the player even through walls. Setting 'Max' to 0 will make mobs not able to see through walls. I recommend using mods like Mobs Properties Randomness to have more control over the attribute; the attribute name is 'enhancedai:generic.xray_follow_range'.")
	public static MinMax xrayRangeOverride = new MinMax(12, 24);
	@Config
	@Label(name = "Instant Target", description = "Mobs will no longer take random time to target a player.")
	public static Boolean instaTarget = false;
	@Config
	@Label(name = "Better Path Finding", description = "Mobs will be able to find better paths to the target. Note that this might hit performance a bit.")
	public static Boolean betterPathfinding = true;
	@Config
	@Label(name = "Hurt by target.Better version", description = "Mobs will no longer switch target if it's the same or if the current one it's closer.")
	public static Boolean betterHurtByTarget = true;
	@Config
	@Label(name = "Hurt by target.Prevent infighting", description = "Mobs will no longer attack each other.")
	public static Boolean preventInfighting = true;
	@Config(min = 0d, max = 1d)
	@Label(name = "Neutral Chances", description = "Chances for a mob to spawn neutral")
	public static Difficulty neutralChances = new Difficulty(0.25d, 0.10d, 0.04d);
	@Config(min = 0d, max = 1d)
	@Label(name = "Blindness range multiplier", description = "If the mobs' affected by blindness effect the target range is multiplied by this value")
	public static Double blindnessRangeMultiplier = .1d;

	public Targeting(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void xrayRangeAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, EAAttributes.XRAY_FOLLOW_RANGE.get()))
				continue;

			event.add(entityType, EAAttributes.XRAY_FOLLOW_RANGE.get(), 0d);
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
		processTargetGoal(mob);
		processHurtByGoal(mob);
	}

	private void processHurtByGoal(Mob mob) {
		if (!betterHurtByTarget
				|| !(mob instanceof PathfinderMob pathfinderMob)
				|| pathfinderMob.getType().is(NO_TARGET_CHANGES))
			return;

		HurtByTargetGoal toRemove = null;
		for (WrappedGoal prioritizedGoal : pathfinderMob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof HurtByTargetGoal goal))
				continue;
			toRemove = goal;

			List<Class<?>> toIgnoreDamage = new ArrayList<>(Arrays.asList(goal.toIgnoreDamage));
			//Prevent infighting
			if (preventInfighting && mob instanceof Enemy)
				toIgnoreDamage.add(Enemy.class);
			EAHurtByTargetGoal newGoal = new EAHurtByTargetGoal(pathfinderMob, toIgnoreDamage.toArray(Class[]::new));
			if (goal.toIgnoreAlert != null)
				newGoal = newGoal.setAlertOthers(goal.toIgnoreAlert);
			pathfinderMob.targetSelector.addGoal(prioritizedGoal.getPriority(), newGoal);

			break;
		}

		if (toRemove != null) {
			mob.targetSelector.removeGoal(toRemove);
		}
		else if (mob.getType().is(ALLOW_TARGET_CHANGE)) {
			List<Class<?>> toIgnoreDamage = new ArrayList<>();
			//Prevent infighting
			if (preventInfighting)
				toIgnoreDamage.add(Enemy.class);
			EAHurtByTargetGoal newGoal = new EAHurtByTargetGoal(pathfinderMob, toIgnoreDamage.toArray(Class[]::new));
			pathfinderMob.targetSelector.addGoal(1, newGoal);
		}
	}

	private void processTargetGoal(Mob mob) {
		if (mob.getType().is(NO_TARGET_CHANGES))
			return;
		List<WrappedGoal> goalsToAdd = new ArrayList<>();

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> goal))
				continue;

			if (goal.targetType != Player.class)
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());

			boolean isNeutral = NBTUtils.getBooleanOrPutDefault(mob.getPersistentData(), IS_NEUTRAL, mob.getRandom().nextDouble() < neutralChances.getByDifficulty(mob.level()));
			if (isNeutral)
				continue;

			EANearestAttackableTarget<Player> newTargetGoal;

			if (mob instanceof Spider)
				newTargetGoal = new EASpiderTargetGoal<>((Spider) mob, Player.class, true, false, goal.targetConditions);
			else
				newTargetGoal = new EANearestAttackableTarget<>(mob, Player.class, false, false, goal.targetConditions);

			if (instaTarget)
				newTargetGoal.setInstaTarget();

			goalsToAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newTargetGoal));
		}

		goalsToRemove.forEach(mob.targetSelector::removeGoal);
		goalsToAdd.forEach(wrappedGoal -> mob.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));

		if (betterPathfinding)
			mob.getNavigation().setMaxVisitedNodesMultiplier(4f);
	}

	private void processFollowRanges(Mob mob) {
		if (mob.getType().is(NO_FOLLOW_RANGE_CHANGES))
			return;
		CompoundTag persistentData = mob.getPersistentData();
		if (!persistentData.getBoolean(FOLLOW_RANGES_PROCESSED)) {
			//noinspection ConstantConditions
			if (followRangeOverride.min != 0d && mob.getAttribute(Attributes.FOLLOW_RANGE) != null && mob.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() < followRangeOverride.min) {
				MCUtils.setAttributeValue(mob, Attributes.FOLLOW_RANGE, followRangeOverride.getIntRandBetween(mob.getRandom()));
			}

			//noinspection ConstantConditions
			if (xrayRangeOverride.min != 0d && mob.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()) != null && mob.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()).getBaseValue() < xrayRangeOverride.min) {
				MCUtils.setAttributeValue(mob, EAAttributes.XRAY_FOLLOW_RANGE.get(), xrayRangeOverride.getIntRandBetween(mob.getRandom()));
			}
			persistentData.putBoolean(FOLLOW_RANGES_PROCESSED, true);
		}
	}

	@SubscribeEvent
	public void onTargetDistanceMultiplier(LivingEvent.LivingVisibilityEvent event) {
		if (!this.isEnabled()
				|| blindnessRangeMultiplier == 1d)
			return;

		if (event.getLookingEntity() instanceof LivingEntity livingEntity && livingEntity.hasEffect(MobEffects.BLINDNESS))
			event.modifyVisibility(blindnessRangeMultiplier);
	}
}
