package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

@Label(name = "Skeleton Flee", description = "Skeletons try to stay away from the target.")
public class SkeletonFleeTarget extends Feature {

    private final ForgeConfigSpec.ConfigValue<Double> avoidPlayerChanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> attackWhenAvoidingChanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeDistanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeDistanceNearConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeSpeedNearConfig;
    private final ForgeConfigSpec.ConfigValue<Double> fleeSpeedFarConfig;
    private final Blacklist.Config entityBlacklistConfig;

    private final List<String> defaultBlacklist = Arrays.asList("quark:forgotten");

    public double avoidPlayerChance = 0.5d;
    public double attackWhenAvoidingChance = 0.5d;
    public double fleeDistance = 16;
    public double fleeDistanceNear = 8;
    public double fleeSpeedNear = 1.6d;
    public double fleeSpeedFar = 1.3d;
    public Blacklist entityBlacklist;

    public SkeletonFleeTarget(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        avoidPlayerChanceConfig = Config.builder
                .comment("Chance for a Skeleton to spawn with the ability to avoid the player")
                .defineInRange("Avoid Player chance", this.avoidPlayerChance, 0d, 1d);
        attackWhenAvoidingChanceConfig = Config.builder
                .comment("Chance for a Skeleton to be able to shoot while running from a player")
                .defineInRange("Attack When Avoiding Chance", this.attackWhenAvoidingChance, 0d, 1d);
        fleeDistanceConfig = Config.builder
                .comment("Distance from a player that will make the skeleton run away.")
                .defineInRange("Flee Distance", this.fleeDistance, 0d, 32d);
        fleeDistanceNearConfig = Config.builder
                .comment("Distance from a player that counts as near and will make the skeleton run away faster.")
                .defineInRange("Flee Distance Near", this.fleeDistanceNear, 0d, 32d);
        fleeSpeedFarConfig = Config.builder
                .comment("Speed multiplier when the skeleton avoids the player and it's farther than 'Flee Distance Near' blocks from him.")
                .defineInRange("Flee speed Multiplier Far", this.fleeSpeedFar, 0d, 4d);
        fleeSpeedNearConfig = Config.builder
                .comment("Speed multiplier when the skeleton avoids the player and it's within 'Flee Distance Near' blocks from him.")
                .defineInRange("Flee speed Multiplier Near", this.fleeSpeedNear, 0d, 4d);
        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the enhanced Shoot AI")
                .setDefaultList(defaultBlacklist)
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.avoidPlayerChance = this.avoidPlayerChanceConfig.get();
        this.attackWhenAvoidingChance = this.attackWhenAvoidingChanceConfig.get();
        this.fleeDistance = this.fleeDistanceConfig.get();
        this.fleeDistanceNear = this.fleeDistanceNearConfig.get();
        this.fleeSpeedNear = this.fleeSpeedNearConfig.get();
        this.fleeSpeedFar = this.fleeSpeedFarConfig.get();
        this.entityBlacklist = this.entityBlacklistConfig.get();
    }

    public void onReassessWeaponGoal(AbstractSkeleton skeleton) {
        if (!this.isEnabled())
            return;

        if (this.entityBlacklist.isEntityBlackOrNotWhitelist(skeleton))
            return;

        boolean avoidTarget = skeleton.level.random.nextDouble() < this.avoidPlayerChance;
        boolean attackWhenAvoiding = skeleton.level.random.nextDouble() < this.attackWhenAvoidingChance;

        CompoundTag persistentData = skeleton.getPersistentData();

        if (persistentData.contains(Strings.Tags.Skeleton.AVOID_TARGET)) {
            avoidTarget = persistentData.getBoolean(Strings.Tags.Skeleton.AVOID_TARGET);
            attackWhenAvoiding = persistentData.getBoolean(Strings.Tags.Skeleton.ATTACK_WHEN_AVOIDING);
        }
        else {
            persistentData.putBoolean(Strings.Tags.Skeleton.AVOID_TARGET, avoidTarget);
            persistentData.putBoolean(Strings.Tags.Skeleton.ATTACK_WHEN_AVOIDING, attackWhenAvoiding);
        }

        if (!avoidTarget)
            return;

        boolean hasAIArrowAttack = false;
        for (WrappedGoal prioritizedGoal : skeleton.goalSelector.availableGoals) {
            if (prioritizedGoal.getGoal().equals(skeleton.bowGoal))
                hasAIArrowAttack = true;
        }
        List<Goal> avoidEntityGoals = skeleton.goalSelector.availableGoals.stream()
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof EAAvoidEntityGoal<?>)
                .toList();

        avoidEntityGoals.forEach(skeleton.goalSelector::removeGoal);
        if (hasAIArrowAttack) {
            EAAvoidEntityGoal<Player> avoidEntityGoal = new EAAvoidEntityGoal<>(skeleton, Player.class, (float) this.fleeDistance, (float) this.fleeDistanceNear, this.fleeSpeedNear, this.fleeSpeedFar);
            avoidEntityGoal.setAttackWhenRunning(attackWhenAvoiding);
            skeleton.goalSelector.addGoal(1, avoidEntityGoal);
        }
    }
}