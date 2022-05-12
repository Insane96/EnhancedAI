package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.EANearestAttackableTargetGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Label(name = "Targeting", description = "Change how mobs target players")
public class Targeting extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> followRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> swimSpeedMultiplierConfig;
	private final ForgeConfigSpec.ConfigValue<Double> xrayConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> instaTargetConfig;
	private final BlacklistConfig entityBlacklistConfig;

	private final List<String> entityBlacklistDefault = Arrays.asList("minecraft:enderman");

	public int followRange = 64;
	public double swimSpeedMultiplier = 2.5d;
	public double xray = 0.15d;
	public boolean instaTarget = true;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist = true;

	public Targeting(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		followRangeConfig = Config.builder
				.comment("How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting to 0 will leave the follow range as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
				.defineInRange("Follow Range Override", this.followRange, 0, 128);
		swimSpeedMultiplierConfig = Config.builder
				.comment("How faster mobs can swim. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
				.defineInRange("Swim Speed Multiplier", this.swimSpeedMultiplier, 0d, 4d);
		xrayConfig = Config.builder
				.comment("Chance for a mob to be able to see players through blocks.")
				.defineInRange("XRay Chance", xray, 0d, 1d);
		instaTargetConfig = Config.builder
				.comment("Mobs will no longer take random time to target a player.")
				.define("Instant Target", instaTarget);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities in here will not have the TargetAI changed", entityBlacklistDefault, false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.followRange = this.followRangeConfig.get();
		this.swimSpeedMultiplier = this.swimSpeedMultiplierConfig.get();
		this.xray = this.xrayConfig.get();
		this.instaTarget = this.instaTargetConfig.get();
		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("6d2cb27e-e5e3-41b9-8108-f74131a90cce");

	@SubscribeEvent
	public void onMobSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Mob mobEntity))
			return;

		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
			if (!this.entityBlacklistAsWhitelist && blacklistEntry.matchesEntity(mobEntity)) {
				isInBlacklist = true;
				break;
			}
			else if (blacklistEntry.matchesEntity(mobEntity)) {
				isInWhitelist = true;
				break;
			}
		}
		if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
			return;

		if (this.followRange != 0) {
			MCUtils.setAttributeValue(mobEntity, Attributes.FOLLOW_RANGE, this.followRange);
		}

		if (this.swimSpeedMultiplier != 0d) {
			MCUtils.applyModifier(mobEntity, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Swim Speed Multiplier", this.swimSpeedMultiplier, AttributeModifier.Operation.MULTIPLY_BASE, false);
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

		goalsToRemove.forEach(mobEntity.goalSelector::removeGoal);

		EANearestAttackableTargetGoal<Player> targetGoal;

		if (mobEntity instanceof Spider)
			targetGoal = new EANearestAttackableTargetGoal.TargetGoal<>((Spider) mobEntity, Player.class, true, false, predicate);
		else
			targetGoal = new EANearestAttackableTargetGoal<>(mobEntity, Player.class, true, false, predicate);
		if (mobEntity.level.random.nextDouble() < this.xray)
			targetGoal.setXray(true);

		targetGoal.setInstaTarget(this.instaTarget);
		mobEntity.targetSelector.addGoal(2, targetGoal);

		EANearestAttackableTargetGoal<Endermite> targetGoalTest;

		if (mobEntity instanceof Spider)
			targetGoalTest = new EANearestAttackableTargetGoal.TargetGoal<>((Spider) mobEntity, Endermite.class, true, false, predicate);
		else
			targetGoalTest = new EANearestAttackableTargetGoal<>(mobEntity, Endermite.class, true, false, predicate);
		if (mobEntity.level.random.nextDouble() < this.xray)
			targetGoalTest.setXray(true);

		targetGoalTest.setInstaTarget(this.instaTarget);
		mobEntity.targetSelector.addGoal(2, targetGoalTest);
	}
}
