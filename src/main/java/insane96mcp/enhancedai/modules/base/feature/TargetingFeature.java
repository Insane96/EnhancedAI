package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.AINearestAttackableTargetGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Label(name = "Targeting", description = "Change how mobs target players")
public class TargetingFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> followRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> xrayConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> instaTargetConfig;
	private final BlacklistConfig entityBlacklistConfig;

	private final List<String> entityBlacklistDefault = Arrays.asList("minecraft:enderman");

	public int followRange = 64;
	public double xray = 0.20d;
	public boolean instaTarget = true;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist = true;

	public TargetingFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		followRangeConfig = Config.builder
				.comment("How far away can the mobs see the player. This overrides the vanilla value (16 for most mobs). Setting to 0 will leave the follow range as vanilla.")
				.defineInRange("Follow Range Override", this.followRange, 0, 128);
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
		this.xray = this.xrayConfig.get();
		this.instaTarget = this.instaTargetConfig.get();
		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Mob))
			return;

		Mob mobEntity = (Mob) event.getEntity();

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

		boolean hasTargetGoal = false;

		Predicate<LivingEntity> predicate = null;

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : mobEntity.targetSelector.availableGoals) {
			if (!(prioritizedGoal.getGoal() instanceof NearestAttackableTargetGoal))
				continue;

			NearestAttackableTargetGoal<?> goal = (NearestAttackableTargetGoal<?>) prioritizedGoal.getGoal();

			if (goal.targetType != Player.class)
				continue;

			predicate = goal.targetConditions.selector;

			goalsToRemove.add(prioritizedGoal.getGoal());
			hasTargetGoal = true;
		}

		if (!hasTargetGoal)
			return;

		goalsToRemove.forEach(mobEntity.goalSelector::removeGoal);

		AINearestAttackableTargetGoal<Player> targetGoal;

		if (mobEntity instanceof Spider)
			targetGoal = new AINearestAttackableTargetGoal.TargetGoal<>((Spider) mobEntity, Player.class, true, false, predicate);
		else
			targetGoal = new AINearestAttackableTargetGoal<>(mobEntity, Player.class, true, false, predicate);
		if (mobEntity.level.random.nextDouble() < this.xray)
			targetGoal.setXray(true);

		targetGoal.setInstaTarget(this.instaTarget);
		mobEntity.targetSelector.addGoal(2, targetGoal);

		AINearestAttackableTargetGoal<Endermite> targetGoalTest;

		if (mobEntity instanceof Spider)
			targetGoalTest = new AINearestAttackableTargetGoal.TargetGoal<>((Spider) mobEntity, Endermite.class, true, false, predicate);
		else
			targetGoalTest = new AINearestAttackableTargetGoal<>(mobEntity, Endermite.class, true, false, predicate);
		if (mobEntity.level.random.nextDouble() < this.xray)
			targetGoalTest.setXray(true);

		targetGoalTest.setInstaTarget(this.instaTarget);
		mobEntity.targetSelector.addGoal(2, targetGoalTest);
	}
}
