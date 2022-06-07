package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.config.IntMinMax;
import insane96mcp.enhancedai.modules.witch.ai.WitchThrowPotionGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.utils.Utils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Label(name = "Witch Potion Throwing", description = "Witches throw potions farther, faster and more potion types. Also no longer chase player if they can't see him.")
public class WitchPotionThrowing extends Feature {

    private final ForgeConfigSpec.ConfigValue<List<? extends String>> badPotionsListConfig;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> goodPotionsListConfig;
    private final ForgeConfigSpec.ConfigValue<Double> lingeringChanceConfig;
    private final ForgeConfigSpec.ConfigValue<Double> anotherThrowChanceConfig;
    private final IntMinMax.Config throwSpeedConfig;
    private final IntMinMax.Config throwRangeConfig;
    private final ForgeConfigSpec.ConfigValue<Boolean> useSlowFallingConfig;
    private final ForgeConfigSpec.ConfigValue<Double> healthThresholdInvisiblityConfig;
    private final BlacklistConfig entityBlacklistConfig;

    public static final List<String> badPotionsListDefault = Arrays.asList("minecraft:weakness,0,1800", "minecraft:slowness,1,1200", "minecraft:hunger,0,600", "minecraft:mining_fatigue,0,600", "minecraft:poison,0,900", "minecraft:blindness,0,120", "minecraft:instant_damage,0,1");
    public static final List<String> goodPotionsListDefault = Arrays.asList("minecraft:regeneration,0,900", "minecraft:speed,0,1800", "minecraft:strength,0,1800", "minecraft:instant_health,0,1");

    public ArrayList<MobEffectInstance> badPotionsList;
    public ArrayList<MobEffectInstance> goodPotionsList;
    public double lingeringChance = 0.15d;
    public double anotherThrowChance = 0.20d;
    public IntMinMax throwSpeed = new IntMinMax(50, 70);
    public IntMinMax throwRange = new IntMinMax(16, 32);
    public boolean useSlowFalling = true;
    public double healthThresholdInvisiblity = 0.50d;
    public ArrayList<IdTagMatcher> entityBlacklist;
    public boolean entityBlacklistAsWhitelist;

    public WitchPotionThrowing(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        this.badPotionsListConfig = Config.builder
                .comment("A list of potions that the witch can throw at enemies. Format is effect_id,amplifier,duration. The potions are applied in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Bad Potions List", badPotionsListDefault, o -> o instanceof String);
        this.goodPotionsListConfig = Config.builder
                .comment("A list of potions that the witch can throw at allies (in raids). Format is effect_id,amplifier,duration. The potions are applied in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Good Potions List", goodPotionsListDefault, o -> o instanceof String);
        this.lingeringChanceConfig = Config.builder
                .comment("Chance for the potions thrown by the Witch to be lingering.")
                .defineInRange("Lingering Chance", this.lingeringChance, 0d, 1d);
        this.anotherThrowChanceConfig = Config.builder
                .comment("Chance for the Witch to throw another random potion right after she threw one.")
                .defineInRange("Another Throw Chance", this.anotherThrowChance, 0d, 1d);
        this.throwSpeedConfig = new IntMinMax.Config(Config.builder, "Throw Speed", "Speed at which Witches throw potions")
                .setMinMax(10, Integer.MAX_VALUE, this.throwSpeed)
                .build();
        this.throwRangeConfig = new IntMinMax.Config(Config.builder, "Throw Range", "Range at which Witches throw potions")
                .setMinMax(8, 64, this.throwRange)
                .build();
        this.useSlowFallingConfig = Config.builder
                .comment("If true, witches will throw a potion of slow falling at their feet when they're falling for more than 8 blocks.")
                .define("Use Slow Falling", this.useSlowFalling);
        this.healthThresholdInvisiblityConfig = Config.builder
                .comment("When below this health percentage Witches will throw Invisibility potions at their feet.")
                .defineInRange("Health Threshold Invisibility", this.healthThresholdInvisiblity, 0d, 1d);
        entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the new Witch ranged attack AI", Collections.emptyList(), false);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.badPotionsList = Utils.parseMobEffectsList(this.badPotionsListConfig.get());
        this.goodPotionsList = Utils.parseMobEffectsList(this.goodPotionsListConfig.get());
        this.lingeringChance = this.lingeringChanceConfig.get();
        this.anotherThrowChance = this.anotherThrowChanceConfig.get();
        this.throwSpeed = this.throwSpeedConfig.get();
        this.throwRange = this.throwRangeConfig.get();
        this.useSlowFalling = this.useSlowFallingConfig.get();
        this.healthThresholdInvisiblity = this.healthThresholdInvisiblityConfig.get();
        this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
        this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (event.getWorld().isClientSide)
            return;

        if (!(event.getEntity() instanceof Witch witch))
            return;

        //Check for black/whitelist
        boolean isInWhitelist = false;
        boolean isInBlacklist = false;
        for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
            if (blacklistEntry.matchesEntity(witch)) {
                if (!this.entityBlacklistAsWhitelist)
                    isInBlacklist = true;
                else
                    isInWhitelist = true;
                break;
            }
        }
        if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
            return;

        List<Goal> rangedAttackGoals = witch.goalSelector.availableGoals.stream()
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof RangedAttackGoal)
                .toList();
        rangedAttackGoals.forEach(witch.goalSelector::removeGoal);

        int attackSpeed = Mth.nextInt(witch.getRandom(), this.throwSpeed.min, this.throwSpeed.max);
        int attackRange = Mth.nextInt(witch.getRandom(), this.throwRange.min, this.throwRange.max);
        witch.goalSelector.addGoal(2, new WitchThrowPotionGoal(witch, attackSpeed, attackSpeed, attackRange));
    }

    public boolean shouldUseSlowFalling() {
        return this.isEnabled() && this.useSlowFalling;
    }
}