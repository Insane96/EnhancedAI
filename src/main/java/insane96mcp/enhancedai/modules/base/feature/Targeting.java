package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.base.ai.EANearestAttackableTarget;
import insane96mcp.enhancedai.modules.base.ai.EASpiderTargetGoal;
import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.*;
import insane96mcp.insanelib.util.IdTagMatcher;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Label(name = "Targeting", description = "Change how mobs target players")
@LoadFeature(module = Modules.Ids.BASE)
public class Targeting extends Feature {

	public static final String IS_NEUTRAL = EnhancedAI.RESOURCE_PREFIX + "is_neutral";

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
	@Label(name = "Prevent Infighting", description = "Mobs will no longer attack each other.")
	public static Boolean preventInfighting = true;
	@Config
	@Label(name = "Entity Blacklist", description = "Entities in here will not be affected by this feature.")
	public static Blacklist entityBlacklist = new Blacklist(List.of(
			new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:enderman")
	), false);
	@Config
	@Label(name = "Neutral Chances", description = "Chances for a mob to spawn neutral")
	public static Difficulty neutralChances = new Difficulty(0.25d, 0.10d, 0.04d);

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
				|| !(event.getEntity() instanceof Mob mobEntity)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(mobEntity))
			return;

		processFollowRanges(mobEntity);
		processTargetGoal(mobEntity);
		processHurtByGoal(mobEntity);
	}

	private void processHurtByGoal(Mob mobEntity) {
		if (!preventInfighting
				|| !(mobEntity instanceof PathfinderMob mob))
			return;

		HurtByTargetGoal toRemove = null;
		for (WrappedGoal prioritizedGoal : mob.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof HurtByTargetGoal goal))
				continue;
			toRemove = goal;

			List<Class<?>> toIgnoreDamage = new ArrayList<>(Arrays.asList(goal.toIgnoreDamage));
			toIgnoreDamage.add(Enemy.class);
			HurtByTargetGoal newGoal = new HurtByTargetGoal(mob, toIgnoreDamage.toArray(Class[]::new));
			if (goal.toIgnoreAlert != null)
				newGoal = newGoal.setAlertOthers(goal.toIgnoreAlert);
			mob.targetSelector.addGoal(prioritizedGoal.getPriority(), newGoal);

			break;
		}

		if (toRemove != null)
			mobEntity.targetSelector.removeGoal(toRemove);
	}

	private void processTargetGoal(Mob mobEntity) {
		List<WrappedGoal> goalsToAdd = new ArrayList<>();

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mobEntity.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> goal))
				continue;

			if (goal.targetType != Player.class)
				continue;

			goalsToRemove.add(prioritizedGoal.getGoal());

			boolean isNeutral = NBTUtils.getBooleanOrPutDefault(mobEntity.getPersistentData(), IS_NEUTRAL, mobEntity.getRandom().nextDouble() < neutralChances.getByDifficulty(mobEntity.level));
			if (isNeutral)
				return;

			EANearestAttackableTarget<Player> newTargetGoal;

			if (mobEntity instanceof Spider)
				newTargetGoal = new EASpiderTargetGoal<>((Spider) mobEntity, Player.class, true, false, goal.targetConditions);
			else
				newTargetGoal = new EANearestAttackableTarget<>(mobEntity, Player.class, false, false, goal.targetConditions);

			if (instaTarget)
				newTargetGoal.setInstaTarget();

			goalsToAdd.add(new WrappedGoal(prioritizedGoal.getPriority(), newTargetGoal));
		}

		goalsToRemove.forEach(mobEntity.targetSelector::removeGoal);
		goalsToAdd.forEach(wrappedGoal -> mobEntity.targetSelector.addGoal(wrappedGoal.getPriority(), wrappedGoal.getGoal()));

		if (betterPathfinding)
			mobEntity.getNavigation().setMaxVisitedNodesMultiplier(4f);
	}

	private void processFollowRanges(Mob mobEntity) {
		CompoundTag persistentData = mobEntity.getPersistentData();
		if (!persistentData.getBoolean(EAStrings.Tags.FOLLOW_RANGES_PROCESSED)) {
			//noinspection ConstantConditions
			if (followRangeOverride.min != 0d && mobEntity.getAttribute(Attributes.FOLLOW_RANGE) != null && mobEntity.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() < followRangeOverride.min) {
				MCUtils.setAttributeValue(mobEntity, Attributes.FOLLOW_RANGE, followRangeOverride.getIntRandBetween(mobEntity.getRandom()));
			}

			//noinspection ConstantConditions
			if (xrayRangeOverride.min != 0d && mobEntity.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()) != null && mobEntity.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()).getBaseValue() < xrayRangeOverride.min) {
				MCUtils.setAttributeValue(mobEntity, EAAttributes.XRAY_FOLLOW_RANGE.get(), xrayRangeOverride.getIntRandBetween(mobEntity.getRandom()));
			}
			persistentData.putBoolean(EAStrings.Tags.FOLLOW_RANGES_PROCESSED, true);
		}
	}
}
