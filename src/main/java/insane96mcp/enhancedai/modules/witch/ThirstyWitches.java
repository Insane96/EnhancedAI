package insane96mcp.enhancedai.modules.witch;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.data.PotionOrMobEffect;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = Modules.Ids.WITCH, description = "Witches drink more potions.")
public class ThirstyWitches extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch/thirsty"));

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> drinkPotionConfig;

    public static final List<String> drinkPotionDefault = List.of("minecraft:strong_swiftness", "minecraft:regeneration");

    public static ArrayList<PotionOrMobEffect> drinkPotion;

	@Config(min = 0d, description = "When witches are this many blocks away from the player will be able to drink the potions in 'Drinkable potions if targeting player'")
	public static Double customDrinkDistanceSafe = 6d;
	@Config(min = 0d, max = 1d, description = "Chance for a witch to drink a healing potion when not full health. Defaults to Vanilla")
	public static Double healingChance = 0.05d;
    @Config(min = 0d, max = 1d, description = "Below this percentage health, witches will try to drink healing potions.")
    public static Double healingThreshold = 0.7d;
    @Config(min = 0d, max = 1d, description = "Below this percentage health, witches will drink strong healing potions instead of normal ones.")
    public static Double strongHealingThreshold = 0.35d;
    @Config(min = 0d, max = 1d, description = "Chance each tick for a witch to drink a water breathing potion when in water and air meter is at half. Vanilla is 15% and doesn't check the air meter.")
    public static Double waterBreathingChance = 1d;
    @Config(min = 0d, max = 1d, description = "Chance each tick for a witch to drink a fire resistance potion when on fire. Vanilla is 15%.")
    public static Double fireResistanceChance = 1d;
	@Config(min = 0d, max = 1d, description = "Chance each tick for a witch to drink milk when they have a negative effect.")
	public static Double milkChance = 0.1d;
	@Config
	public static Boolean playSoundWhenDrinking = true;

	public static EAIData<Double> CUSTOM_DRINK_DISTANCE_SAFE;
	public static EAIData<Double> HEALING_CHANCE;
	public static EAIData<Double> HEALING_THRESHOLD;
	public static EAIData<Double> STRONG_HEALING_THRESHOLD;
	public static EAIData<Double> WATER_BREATHING_CHANCE;
	public static EAIData<Double> FIRE_RESISTANCE_CHANCE;
	public static EAIData<Double> MILK_CHANCE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CUSTOM_DRINK_DISTANCE_SAFE = EAIData.ofDouble(this.createDataKey("custom_drink_distance_safe"));
		HEALING_CHANCE = EAIData.ofDouble(this.createDataKey("healing_chance"));
		HEALING_THRESHOLD = EAIData.ofDouble(this.createDataKey("healing_threshold"));
		STRONG_HEALING_THRESHOLD = EAIData.ofDouble(this.createDataKey("strong_healing_threshold"));
		WATER_BREATHING_CHANCE = EAIData.ofDouble(this.createDataKey("water_breathing_chance"));
		FIRE_RESISTANCE_CHANCE = EAIData.ofDouble(this.createDataKey("fire_resistance_chance"));
		MILK_CHANCE = EAIData.ofDouble(this.createDataKey("milk_chance"));
    }

    @Override
    public void loadConfigOptions() {
        super.loadConfigOptions();
        drinkPotionConfig = this.getBuilder()
                .comment("A list of potions that the witch will drink when a player is targeted and it's at least 6 blocks away. Format is potion_id or effect_id,duration,amplifier. The potions are applied in order and witches will not drink the same potion if already has the effect.")
                .defineList("Drinkable potions if targeting player", drinkPotionDefault, o -> o instanceof String);
    }

    @Override
    public void readConfig(final ModConfigEvent event) {
        super.readConfig(event);
        drinkPotion = PotionOrMobEffect.parseList(drinkPotionConfig.get());
    }

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Witch witch)
				|| !witch.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		CUSTOM_DRINK_DISTANCE_SAFE.applyIfAbsent(witch, customDrinkDistanceSafe);
		HEALING_CHANCE.applyIfAbsent(witch, healingChance);
		HEALING_THRESHOLD.applyIfAbsent(witch, healingThreshold);
		STRONG_HEALING_THRESHOLD.applyIfAbsent(witch, strongHealingThreshold);
		WATER_BREATHING_CHANCE.applyIfAbsent(witch, waterBreathingChance);
		FIRE_RESISTANCE_CHANCE.applyIfAbsent(witch, fireResistanceChance);
		MILK_CHANCE.applyIfAbsent(witch, milkChance);
	}
}
