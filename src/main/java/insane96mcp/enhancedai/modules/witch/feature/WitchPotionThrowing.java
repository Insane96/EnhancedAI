package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.witch.ai.WitchThrowPotionGoal;
import insane96mcp.enhancedai.modules.witch.data.PotionOrMobEffect;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Label(name = "Witch Potion Throwing", description = "Witches throw potions farther, faster and more potion types. Also no longer chase player if they can't see him.")
@LoadFeature(module = Modules.Ids.WITCH)
public class WitchPotionThrowing extends Feature {

    public static final String APPRENTICE = EnhancedAI.RESOURCE_PREFIX + "apprentice";
    public static final String ATTACK_SPEED = EnhancedAI.RESOURCE_PREFIX + "attack_speed";
    public static final String ATTACK_RANGE = EnhancedAI.RESOURCE_PREFIX + "attack_range";
    public static final String LINGERING_CHANCE = EnhancedAI.RESOURCE_PREFIX + "lingering_chance";
    public static final String ANOTHER_THROW_CHANCE = EnhancedAI.RESOURCE_PREFIX + "another_throw_chance";

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> badPotionsListConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> goodPotionsListConfig;
    public static final List<String> badPotionsListDefault = List.of("minecraft:weakness", "minecraft:slowness", "minecraft:hunger,600,0", "minecraft:mining_fatigue,600,0", "minecraft:poison", "minecraft:blindness,120,0", "minecraft:strong_harming");
    public static final List<String> goodPotionsListDefault = List.of("minecraft:regeneration", "minecraft:swiftness", "minecraft:strength", "minecraft:healing");

    public static ArrayList<PotionOrMobEffect> badPotionsList;
    public static ArrayList<PotionOrMobEffect> goodPotionsList;

    @Config(min = 0d, max = 1d)
    @Label(name = "Lingering Chance", description = "Chance for the potions thrown by the Witch to be lingering.")
    public static Double lingeringChance = 0.15d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Another Throw Chance", description = "Chance for the Witch to throw another random potion right after she threw one.")
    public static Double anotherThrowChance = 0.20d;
    @Config(min = 1)
    @Label(name = "Throw Speed", description = "Speed at which Witches throw potions (in ticks).")
    public static MinMax throwSpeed = new MinMax(70, 90);
    @Config(min = 8, max = 64)
    @Label(name = "Throw Range", description = "Range at which Witches throw potions.")
    public static MinMax throwRange = new MinMax(16, 24);
    @Config(min = 0d, max = 1d)
    @Label(name = "Apprentice Witch.Chance", description = "Chance for a Witch to be an apprentice. Apprentice Witches throw random potions instead of in order, and have a chance to throw a wrong (good) potion.")
    public static Double apprenticeWitchChance = 0.6d;
    @Config
    @Label(name = "Use Slow Falling", description = "If true, witches will throw a potion of slow falling at their feet when they're falling for more than 8 blocks.")
    public static Boolean useSlowFalling = true;
    @Config(min = 0d, max = 1d)
    @Label(name = "Health Threshold Invisibility", description = "When below this health percentage Witches will throw Invisibility potions at their feet.")
    public static Double healthThresholdInvisibility = 0.40d;
    @Config
    @Label(name = "Entity Blacklist", description = "Entities that will not get affected by this feature")
    public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

    public WitchPotionThrowing(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public void loadConfigOptions() {
        super.loadConfigOptions();
        badPotionsListConfig = this.getBuilder()
                .comment("A list of potions that the witch can throw at enemies. Format is effect_id,duration,amplifier. The potions are thrown in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Bad Potions List", badPotionsListDefault, o -> o instanceof String);
        goodPotionsListConfig = this.getBuilder()
                .comment("A list of potions that the witch can throw at allies (in raids). Format is effect_id,duration,amplifier. The potions are thrown in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Good Potions List", goodPotionsListDefault, o -> o instanceof String);
    }

    @Override
    public void readConfig(final ModConfigEvent event) {
        super.readConfig(event);
        badPotionsList = PotionOrMobEffect.parseList(badPotionsListConfig.get());
        goodPotionsList = PotionOrMobEffect.parseList(goodPotionsListConfig.get());
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Witch witch)
                || entityBlacklist.isEntityBlackOrNotWhitelist(witch))
            return;

        CompoundTag persistentData = witch.getPersistentData();
        int attackSpeed = NBTUtils.getIntOrPutDefault(persistentData, ATTACK_SPEED, throwSpeed.getIntRandBetween(witch.getRandom()));
        int attackRange = NBTUtils.getIntOrPutDefault(persistentData, ATTACK_RANGE, throwRange.getIntRandBetween(witch.getRandom()));
        double lingeringChance1 = NBTUtils.getDoubleOrPutDefault(persistentData, LINGERING_CHANCE, lingeringChance);
        double anotherThrowChance1 = NBTUtils.getDoubleOrPutDefault(persistentData, ANOTHER_THROW_CHANCE, anotherThrowChance);
        boolean apprentice = NBTUtils.getBooleanOrPutDefault(persistentData, APPRENTICE, witch.getRandom().nextDouble() < apprenticeWitchChance);

        List<Goal> rangedAttackGoals = witch.goalSelector.availableGoals.stream()
                .map(WrappedGoal::getGoal)
                .filter(g -> g instanceof RangedAttackGoal)
                .toList();
        rangedAttackGoals.forEach(witch.goalSelector::removeGoal);

        witch.goalSelector.addGoal(2, new WitchThrowPotionGoal(witch, attackSpeed, attackSpeed, attackRange, lingeringChance1, anotherThrowChance1, apprentice));
        //witch.targetSelector.addGoal(2, new WitchBuffAllyGoal<>(witch, Mob.class, true, (livingEntity -> livingEntity != null && !witch.hasActiveRaid() && livingEntity.getType() != EntityType.WITCH)));
    }

    public static boolean shouldUseSlowFalling() {
        return isEnabled(WitchPotionThrowing.class) && useSlowFalling;
    }
}