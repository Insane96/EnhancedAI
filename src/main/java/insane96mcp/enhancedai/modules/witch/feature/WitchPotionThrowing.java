package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.modules.witch.ai.WitchThrowPotionGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
    private final MinMax.Config throwSpeedConfig;
    private final MinMax.Config throwRangeConfig;
    private final ForgeConfigSpec.ConfigValue<Boolean> useSlowFallingConfig;
    private final ForgeConfigSpec.ConfigValue<Double> healthThresholdInvisiblityConfig;
    private final Blacklist.Config entityBlacklistConfig;

    public static final List<String> badPotionsListDefault = Arrays.asList("minecraft:weakness,1800,0", "minecraft:slowness,1200,1", "minecraft:hunger,600,0", "minecraft:mining_fatigue,600,0", "minecraft:poison,900,0", "minecraft:blindness,120,0", "minecraft:instant_damage,1,0");
    public static final List<String> goodPotionsListDefault = Arrays.asList("minecraft:regeneration,900,0", "minecraft:speed,1800,0", "minecraft:strength,1800,0", "minecraft:instant_health,1,0");

    public ArrayList<MobEffectInstance> badPotionsList;
    public ArrayList<MobEffectInstance> goodPotionsList;
    public double lingeringChance = 0.15d;
    public double anotherThrowChance = 0.20d;
    public MinMax throwSpeed = new MinMax(50, 70);
    public MinMax throwRange = new MinMax(16, 32);
    public boolean useSlowFalling = true;
    public double healthThresholdInvisiblity = 0.50d;
    public Blacklist entityBlacklist;

    public WitchPotionThrowing(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        this.badPotionsListConfig = Config.builder
                .comment("A list of potions that the witch can throw at enemies. Format is effect_id,duration,amplifier. The potions are applied in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Bad Potions List", badPotionsListDefault, o -> o instanceof String);
        this.goodPotionsListConfig = Config.builder
                .comment("A list of potions that the witch can throw at allies (in raids). Format is effect_id,duration,amplifier. The potions are applied in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Good Potions List", goodPotionsListDefault, o -> o instanceof String);
        this.lingeringChanceConfig = Config.builder
                .comment("Chance for the potions thrown by the Witch to be lingering.")
                .defineInRange("Lingering Chance", this.lingeringChance, 0d, 1d);
        this.anotherThrowChanceConfig = Config.builder
                .comment("Chance for the Witch to throw another random potion right after she threw one.")
                .defineInRange("Another Throw Chance", this.anotherThrowChance, 0d, 1d);
        this.throwSpeedConfig = new MinMax.Config(Config.builder, "Throw Speed", "Speed at which Witches throw potions")
                .setMinMax(10, Integer.MAX_VALUE, this.throwSpeed)
                .build();
        this.throwRangeConfig = new MinMax.Config(Config.builder, "Throw Range", "Range at which Witches throw potions")
                .setMinMax(8, 64, this.throwRange)
                .build();
        this.useSlowFallingConfig = Config.builder
                .comment("If true, witches will throw a potion of slow falling at their feet when they're falling for more than 8 blocks.")
                .define("Use Slow Falling", this.useSlowFalling);
        this.healthThresholdInvisiblityConfig = Config.builder
                .comment("When below this health percentage Witches will throw Invisibility potions at their feet.")
                .defineInRange("Health Threshold Invisibility", this.healthThresholdInvisiblity, 0d, 1d);
        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the new Witch ranged attack AI")
                .setDefaultList(Collections.emptyList())
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.badPotionsList = MCUtils.parseMobEffectsList(this.badPotionsListConfig.get());
        this.goodPotionsList = MCUtils.parseMobEffectsList(this.goodPotionsListConfig.get());
        this.lingeringChance = this.lingeringChanceConfig.get();
        this.anotherThrowChance = this.anotherThrowChanceConfig.get();
        this.throwSpeed = this.throwSpeedConfig.get();
        this.throwRange = this.throwRangeConfig.get();
        this.useSlowFalling = this.useSlowFallingConfig.get();
        this.healthThresholdInvisiblity = this.healthThresholdInvisiblityConfig.get();
        this.entityBlacklist = this.entityBlacklistConfig.get();
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled()
                || event.getWorld().isClientSide
                || !(event.getEntity() instanceof Witch witch)
                || this.entityBlacklist.isEntityBlackOrNotWhitelist(witch))
            return;

        CompoundTag persistentData = witch.getPersistentData();
        int attackSpeed = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Witch.ATTACK_SPEED, this.throwSpeed.getIntRandBetween(witch.getRandom()));
        int attackRange = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Witch.ATTACK_RANGE, this.throwRange.getIntRandBetween(witch.getRandom()));
        double lingeringChance = NBTUtils.getDoubleOrPutDefault(persistentData, EAStrings.Tags.Witch.LINGERING_CHANCE, this.lingeringChance);
        double anotherThrowChance = NBTUtils.getDoubleOrPutDefault(persistentData, EAStrings.Tags.Witch.ANOTHER_THROW_CHANCE, this.anotherThrowChance);

        List<Goal> rangedAttackGoals = witch.goalSelector.availableGoals.stream()
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof RangedAttackGoal)
                .toList();
        rangedAttackGoals.forEach(witch.goalSelector::removeGoal);

        witch.goalSelector.addGoal(2, new WitchThrowPotionGoal(witch, attackSpeed, attackSpeed, attackRange, lingeringChance, anotherThrowChance));
        //witch.targetSelector.addGoal(2, new WitchBuffAllyGoal<>(witch, Mob.class, true, (livingEntity -> livingEntity != null && !witch.hasActiveRaid() && livingEntity.getType() != EntityType.WITCH)));
    }

    public boolean shouldUseSlowFalling() {
        return this.isEnabled() && this.useSlowFalling;
    }
}