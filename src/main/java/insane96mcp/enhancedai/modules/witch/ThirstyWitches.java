package insane96mcp.enhancedai.modules.witch;

import insane96mcp.enhancedai.data.PotionOrMobEffect;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Thirsty Witches", description = "Witches drink more potions.")
@LoadFeature(module = Modules.Ids.WITCH)
public class ThirstyWitches extends Feature {
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> drinkPotionConfig;

    public static final List<String> drinkPotionDefault = List.of("minecraft:strong_swiftness", "minecraft:regeneration");

    public static ArrayList<PotionOrMobEffect> drinkPotion;

    @Config(min = 0d, max = 1d)
    @Label(name = "Healing Chance", description = "Chance for a witch to drink a healing potion when not full health. Defaults to Vanilla")
    public static Double healingChance = 0.05d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Healing Threshold", description = "Below this percentage health, witches will try to drink healing potions.")
    public static Double healingThreshold = 0.7d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Strong healing Threshold", description = "Below this percentage health, witches will drink strong healing potions instead of normal ones.")
    public static Double strongHealingThreshold = 0.4d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Water Breathing Chance", description = "Chance each tick for a witch to drink a water breathing potion when in water and air meter is at half. Vanilla is 15% and doesn't check the air meter.")
    public static Double waterBreathingChance = 1d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Fire Resistance Chance", description = "Chance each tick for a witch to drink a fire resistance potion when on fire. Vanilla is 15%.")
    public static Double fireResistanceChance = 1d;
    @Config(min = 0d, max = 1d)
    @Label(name = "Drink Milk Chance", description = "Chance each tick for a witch to drink milk when they have a negative effect.")
    public static Double milkChance = 0.1d;

    public ThirstyWitches(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public void loadConfigOptions() {
        super.loadConfigOptions();
        drinkPotionConfig = this.getBuilder()
                .comment("A list of potions that the witch will drink as soon as the player is targeted. Note that witches can still drink other potions in different situations, refer to other config options. Format is effect_id,duration,amplifier. The potions are applied in order and witches will not drink the same potion if already has the effect.")
                .defineList("Potions on Target List", drinkPotionDefault, o -> o instanceof String);
    }

    @Override
    public void readConfig(final ModConfigEvent event) {
        super.readConfig(event);
        drinkPotion = PotionOrMobEffect.parseList(drinkPotionConfig.get());
    }

    //TODO Set chances and threshold as NBT
}
