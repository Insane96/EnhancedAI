package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.utils.Utils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Label(name = "Thirsty Witches", description = "Witches drink more potions.")
public class ThirstyWitches extends Feature {

    private final ForgeConfigSpec.ConfigValue<List<? extends String>> drinkPotionConfig;
    private final ForgeConfigSpec.DoubleValue healingChanceConfig;
    private final ForgeConfigSpec.DoubleValue strongHealingThresholdConfig;
    private final ForgeConfigSpec.DoubleValue waterBreathingChanceConfig;
    private final ForgeConfigSpec.DoubleValue fireResistanceChanceConfig;
    private final ForgeConfigSpec.DoubleValue milkChanceConfig;

    public static final List<String> drinkPotionDefault = Arrays.asList("minecraft:speed,1,3600", "minecraft:resistance,0,3600", "minecraft:absorption,0,1800", "minecraft:regeneration,0,900");

    public ArrayList<MobEffectInstance> drinkPotion;
    public double healingChance = 0.05d;
    public double strongHealingThreshold = 0.4d;
    public double waterBreathingChance = 1d;
    public double fireResistanceChance = 1d;
    public double milkChance = 0.1d;

    public ThirstyWitches(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        this.drinkPotionConfig = Config.builder
                .comment("A list of potions that the witch will drink as soon as the player is targeted. Note that witches can still drink other potions in different situations, refer to other config options. Format is effect_id,amplifier,duration. The potions are applied in order and witches will not throw the same potion if the target has already the effect.")
                .defineList("Potions on Target List", drinkPotionDefault, o -> o instanceof String);
        this.healingChanceConfig = Config.builder
                .comment("Chance for a witch to drink a healing potion when not full health. Defaults to Vanilla")
                .defineInRange("Healing Chance", this.healingChance, 0d, 1d);
        this.strongHealingThresholdConfig = Config.builder
                .comment("Below this percentage health, witches will drink strong healing potions instead of normal ones.")
                .defineInRange("Strong healing Threshold", this.strongHealingThreshold, 0d, 1d);
        this.waterBreathingChanceConfig = Config.builder
                .comment("Chance for a witch to drink a water breathing potion when in water and air meter is at half. Vanilla is 15% and doesn't check the air meter.")
                .defineInRange("Water Breathing Chance", this.waterBreathingChance, 0d, 1d);
        this.fireResistanceChanceConfig = Config.builder
                .comment("Chance for a witch to drink a fire resistance potion when on fire. Vanilla is 15%.")
                .defineInRange("Fire Resistance", this.fireResistanceChance, 0d, 1d);
        this.milkChanceConfig = Config.builder
                .comment("Chance for a witch to drink milk when they have a negative effect.")
                .defineInRange("Drink Milk Chance", this.milkChance, 0d, 1d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.drinkPotion = Utils.parseMobEffectsList(this.drinkPotionConfig.get());
        this.healingChance = this.healingChanceConfig.get();
        this.waterBreathingChance = this.waterBreathingChanceConfig.get();
        this.fireResistanceChance = this.fireResistanceChanceConfig.get();
        this.milkChance = this.milkChanceConfig.get();
    }

    public boolean shouldDrinkMilk(Random random) {
        return this.isEnabled() && random.nextDouble() < this.milkChance;
    }
}
