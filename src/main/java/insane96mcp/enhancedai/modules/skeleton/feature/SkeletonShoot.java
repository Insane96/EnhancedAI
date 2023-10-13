package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.skeleton.ai.EARangedBowAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

@Label(name = "Skeleton Shoot", description = "Skeletons are more precise when shooting and strafing is removed, can hit a target from up to 64 blocks and try to stay away from the target.")
public class SkeletonShoot extends Feature {

	public static final TagKey<Item> BOWS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(EnhancedAI.MOD_ID, "bows"));

	public static final String STRAFE = EnhancedAI.RESOURCE_PREFIX + "strafe";
	public static final String SHOOTING_RANGE = EnhancedAI.RESOURCE_PREFIX + "shooting_range";
	public static final String SHOOTING_COOLDOWN = EnhancedAI.RESOURCE_PREFIX + "shooting_cooldown";
	public static final String BOW_CHARGE_TICKS = EnhancedAI.RESOURCE_PREFIX + "bow_charge_ticks";
	public static final String INACCURACY = EnhancedAI.RESOURCE_PREFIX + "inaccuracy";
	private static final String SPAMMER = EnhancedAI.RESOURCE_PREFIX + "spammer";

	private final MinMax.Config shootingRangeConfig;
	private final ForgeConfigSpec.ConfigValue<Double> strafeChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> arrowInaccuracyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> spammerChanceConfig;
	private final MinMax.Config shootingCooldownConfig;
	private final MinMax.Config bowChargeTicksConfig;
	private final Blacklist.Config entityBlacklistConfig;

	private final List<String> defaultBlacklist = List.of("quark:forgotten");

	public MinMax shootingRange = new MinMax(24, 32);
	public double strafeChance = 0.333d;
	public double arrowInaccuracy = 4d;
	public double spammerChance = 0.07d;
	public MinMax shootingCooldown = new MinMax(40, 60);
	public MinMax bowChargeTicks = new MinMax(15, 30);
	public Blacklist entityBlacklist;

	public SkeletonShoot(Module module) {
		super(Config.builder, module);
		super.pushConfig(Config.builder);
		this.shootingRangeConfig = new MinMax.Config(Config.builder, "Shooting Range", "The range from where a skeleton will shoot a player")
				.setMinMax(1, 64, this.shootingRange)
				.build();
		strafeChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn with the ability to strafe (like vanilla)")
				.defineInRange("Strafe chance", this.strafeChance, 0d, 1d);
		arrowInaccuracyConfig = Config.builder
				.comment("How much inaccuracy does the arrow fired by skeletons have. Vanilla skeletons have 10/6/2 inaccuracy in easy/normal/hard difficulty.")
				.defineInRange("Arrow Inaccuracy", this.arrowInaccuracy, 0d, 30d);
		spammerChanceConfig = Config.builder
				.comment("Chance for a Skeleton to spawn as a spammer, which spams arrows instead of fully charging the bow")
				.defineInRange("Spammer chance", this.spammerChance, 0d, 1d);
		this.shootingCooldownConfig = new MinMax.Config(Config.builder, "Shooting Cooldown", "The ticks cooldown after shooting. This is halved in Hard difficulty")
				.setMinMax(1, 1200, this.shootingCooldown)
				.build();
		this.bowChargeTicksConfig = new MinMax.Config(Config.builder, "Bow charge ticks", "The ticks the skeleton charges the bow. at least 20 ticks for a full charge.")
				.setMinMax(1, 1200, this.bowChargeTicks)
				.build();
		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the enhanced Shoot AI")
				.setDefaultList(defaultBlacklist)
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.shootingRange = this.shootingRangeConfig.get();
		this.strafeChance = this.strafeChanceConfig.get();
		this.arrowInaccuracy = this.arrowInaccuracyConfig.get();
		this.spammerChance = this.spammerChanceConfig.get();
		this.shootingCooldown = this.shootingCooldownConfig.get();
		this.bowChargeTicks = this.bowChargeTicksConfig.get();
		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	public void onReassessWeaponGoal(AbstractSkeleton skeleton) {
		if (!this.isEnabled())
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(skeleton))
			return;

		CompoundTag persistentData = skeleton.getPersistentData();

		boolean strafe = NBTUtils.getBooleanOrPutDefault(persistentData, STRAFE, skeleton.level.random.nextDouble() < this.strafeChance);
		int shootingRange = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_RANGE, this.shootingRange.getIntRandBetween(skeleton.getRandom()));
		boolean spammer = NBTUtils.getBooleanOrPutDefault(persistentData, SPAMMER, skeleton.level.random.nextDouble() < this.spammerChance);
		int shootingCooldown1 = NBTUtils.getIntOrPutDefault(persistentData, SHOOTING_COOLDOWN, shootingCooldown.getIntRandBetween(skeleton.getRandom()));
		int bowChargeTicks1 = NBTUtils.getIntOrPutDefault(persistentData, BOW_CHARGE_TICKS, bowChargeTicks.getIntRandBetween(skeleton.getRandom()));

		boolean hasAIArrowAttack = false;
		for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
			if (prioritizedGoal.getGoal().equals(skeleton.bowGoal))
				hasAIArrowAttack = true;
		}
		List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
				.map(WrappedGoal::getGoal)
				.filter(g -> g instanceof EAAvoidEntityGoal<?> || g.equals(skeleton.bowGoal))
				.toList();

		avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
		if (!hasAIArrowAttack && skeleton.isHolding(stack -> stack.is(BOWS))) {
			hasAIArrowAttack = true;
			skeleton.goalSelector.removeGoal(skeleton.meleeGoal);
		}
		if (hasAIArrowAttack) {
			double inaccuracy = this.arrowInaccuracy;
			if (spammer) {
				shootingCooldown1 = 30;
				bowChargeTicks1 = 1;
				inaccuracy *= 2.5d;
			}
			if (skeleton.level.getDifficulty().equals(Difficulty.HARD))
				shootingCooldown1 /= 2;

			EARangedBowAttackGoal<AbstractSkeleton> EARangedBowAttackGoal = new EARangedBowAttackGoal<>(skeleton, 1.0d, shootingRange, strafe)
					.setAttackCooldown(shootingCooldown1)
					.setBowChargeTicks(bowChargeTicks1)
					.setInaccuracy((float) inaccuracy);
			skeleton.goalSelector.addGoal(2, EARangedBowAttackGoal);
		}
	}
}