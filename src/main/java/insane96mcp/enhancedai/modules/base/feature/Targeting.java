package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.EANearestAttackableTarget;
import insane96mcp.enhancedai.modules.base.ai.EASpiderTargetGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Label(name = "Targeting", description = "Change how mobs target players")
public class Targeting extends Feature {

	private final MinMax.Config followRangeOverrideConfig;
	private final MinMax.Config xRayRangeOverrideConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> instaTargetConfig;
	private final ForgeConfigSpec.BooleanValue betterPathfindingConfig;

	private final Blacklist.Config entityBlacklistConfig;

	private final List<String> entityBlacklistDefault = List.of("minecraft:enderman");

	public MinMax followRangeOverride = new MinMax(32, 64);
	public MinMax xrayRangeOverride = new MinMax(16, 32);
	public boolean instaTarget = true;
	public boolean betterPathfinding = true;

	public Blacklist entityBlacklist;

	public Targeting(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		followRangeOverrideConfig = new MinMax.Config(Config.builder, "Follow Range Override", "How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting 'Max' to 0 will leave the follow range as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
				.setMinMax(0, 128,  this.followRangeOverride)
				.build();
		xRayRangeOverrideConfig = new MinMax.Config(Config.builder, "XRay Range Override", "How far away can the mobs see the player even through walls. Setting 'Max' to 0 will make mobs not able to see through walls. I recommend using mods like Mobs Properties Randomness to have more control over the attribute; the attribute name is 'enhancedai:generic.xray_follow_range'.")
				.setMinMax(0, 128,  this.xrayRangeOverride)
				.build();
		instaTargetConfig = Config.builder
				.comment("Mobs will no longer take random time to target a player.")
				.define("Instant Target", instaTarget);
		betterPathfindingConfig = Config.builder
				.comment("Mobs will be able to find better paths to the target. Note that this might hit performance a bit.")
				.define("Better Path Finding", this.betterPathfinding);

		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities in here will not have the TargetAI changed")
				.setDefaultList(entityBlacklistDefault)
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.followRangeOverride = this.followRangeOverrideConfig.get();
		this.xrayRangeOverride = this.xRayRangeOverrideConfig.get();
		this.instaTarget = this.instaTargetConfig.get();
		this.betterPathfinding = this.betterPathfindingConfig.get();

		this.entityBlacklist = this.entityBlacklistConfig.get();
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
	public void onMobSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Mob mobEntity))
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(mobEntity))
			return;

		CompoundTag persistentData = mobEntity.getPersistentData();
		if (!persistentData.getBoolean(Strings.Tags.FOLLOW_RANGES_PROCESSED)) {
			//noinspection ConstantConditions
			if (this.followRangeOverride.min != 0d && mobEntity.getAttribute(Attributes.FOLLOW_RANGE) != null && mobEntity.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() < this.followRangeOverride.min) {
				MCUtils.setAttributeValue(mobEntity, Attributes.FOLLOW_RANGE, this.followRangeOverride.getIntRandBetween(mobEntity.getRandom()));
			}

			//noinspection ConstantConditions
			if (this.xrayRangeOverride.min != 0d && mobEntity.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()) != null && mobEntity.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()).getBaseValue() < this.xrayRangeOverride.min) {
				MCUtils.setAttributeValue(mobEntity, EAAttributes.XRAY_FOLLOW_RANGE.get(), this.xrayRangeOverride.getIntRandBetween(mobEntity.getRandom()));
			}
			persistentData.putBoolean(Strings.Tags.FOLLOW_RANGES_PROCESSED, true);
		}

		boolean hasTargetGoal = false;

		Predicate<LivingEntity> predicate = null;

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mobEntity.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal<?> goal))
				continue;

			if (goal.targetType != Player.class)
				continue;

			predicate = goal.targetConditions.selector;

			goalsToRemove.add(prioritizedGoal.getGoal());
			hasTargetGoal = true;
		}

		if (!hasTargetGoal)
			return;

		goalsToRemove.forEach(mobEntity.targetSelector::removeGoal);

		EANearestAttackableTarget<Player> targetGoal;

		if (mobEntity instanceof Spider)
			targetGoal = new EASpiderTargetGoal<>((Spider) mobEntity, Player.class, true, false, predicate);
		else
			targetGoal = new EANearestAttackableTarget<>(mobEntity, Player.class, false, false, predicate);

		if (this.instaTarget)
			targetGoal.setInstaTarget();
		mobEntity.targetSelector.addGoal(2, targetGoal);
		if (this.betterPathfinding)
			mobEntity.getNavigation().setMaxVisitedNodesMultiplier(4f);

		/*ILNearestAttackableTargetGoal<Endermite> targetGoalTest;

		if (mobEntity instanceof Spider)
			targetGoalTest = new EASpiderTargetGoal<>((Spider) mobEntity, Endermite.class, true, false, predicate);
		else
			targetGoalTest = new EANearestAttackableTarget<>(mobEntity, Endermite.class, false, false, predicate);

		mobEntity.targetSelector.addGoal(2, targetGoalTest.setInstaTarget());*/
	}
}
