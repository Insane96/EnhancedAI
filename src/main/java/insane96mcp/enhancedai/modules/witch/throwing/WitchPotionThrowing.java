package insane96mcp.enhancedai.modules.witch.throwing;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.data.PotionOrMobEffect;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = Modules.Ids.WITCH, description = "Witches throw potions farther, faster and more potion types. Also no longer chase player if they can't see him. Use the enhancedai:witch/better_potion_throwing entity type tag to add more witches that are affected by this feature.")
public class WitchPotionThrowing extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch/better_potion_throwing"));

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> badPotionsListConfig;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> goodPotionsListConfig;
    public static final List<String> badPotionsListDefault = List.of("minecraft:weakness", "minecraft:slowness", "minecraft:hunger,600,0", "minecraft:mining_fatigue,600,0", "minecraft:poison", "minecraft:blindness,120,0", "minecraft:harming");
    public static final List<String> goodPotionsListDefault = List.of("minecraft:regeneration", "minecraft:swiftness", "minecraft:strength", "minecraft:healing", "minecraft:invisibility");

    public static ArrayList<PotionOrMobEffect> badPotionsList;
    public static ArrayList<PotionOrMobEffect> goodPotionsList;

    @Config(min = 0d, max = 1d, description = "Chance for the potions thrown by the Witch to be lingering.")
    public static Double lingeringChance = 0.15d;
    @Config(min = 1, description = "Speed at which Witches throw potions (in ticks).")
    public static MinMax attackCooldown = new MinMax(70, 90);
    @Config(min = 8, max = 64, description = "Range at which Witches throw potions.")
    public static MinMax attackRange = new MinMax(16, 24);
	@Config(min = 0d)
	public static Double inaccuracy = 1d;
	@Config(min = 0d, max = 1d, description = "Chance for a Witch to be an apprentice. Apprentice Witches throw random potions instead of in order, and have a chance to throw a wrong (good) potion.")
	public static Double apprenticeChance = 0.5d;
    @Config(description = "If true, witches will throw a potion of slow falling at their feet when they're falling for more than 8 blocks.")
    public static Boolean useSlowFalling = true;
    @Config(min = 0d, max = 1d, description = "When below this health percentage Witches will throw Invisibility potions at their feet.")
    public static Double invisibilityHealthThreshold = 0.40d;

    public static ResourceLocation INVISIBILITY_COOLDOWN;
	public static EAIData<Double> LINGERING_CHANCE;
	public static EAIData<Integer> ATTACK_COOLDOWN;
	public static EAIData<Integer> ATTACK_RANGE;
	public static EAIData<Double> INACCURACY;
	public static EAIData<Boolean> APPRENTICE;
	public static EAIData<Boolean> USE_SLOW_FALL;
	public static EAIData<Double> INVISIBILITY_HEALTH_THRESHOLD;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        INVISIBILITY_COOLDOWN = this.createDataKey("invisibility_cooldown");
		LINGERING_CHANCE = EAIData.ofDouble(this.createDataKey("lingering_chance"));
		ATTACK_COOLDOWN = EAIData.ofInt(this.createDataKey("attack_cooldown"));
		ATTACK_RANGE = EAIData.ofInt(this.createDataKey("attack_range"));
		INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
		APPRENTICE = EAIData.ofBool(this.createDataKey("apprentice"));
		USE_SLOW_FALL = EAIData.ofBool(this.createDataKey("use_slow_fall"));
		INVISIBILITY_HEALTH_THRESHOLD = EAIData.ofDouble(this.createDataKey("invisibility_health_threshold"));
    }

    @Override
    public void loadConfigOptions() {
        super.loadConfigOptions();
        badPotionsListConfig = this.getBuilder()
                .comment("A list of potions that the witch can throw at enemies. Format is effect_id,duration,amplifier. The potions are thrown in order and witches will not throw a potion if the target has already the effect.")
                .defineList("Bad Potions List", badPotionsListDefault, o -> o instanceof String);
        goodPotionsListConfig = this.getBuilder()
                .comment("A list of potions that the witch can throw at allies (in raids). Format is effect_id,duration,amplifier. The potions are thrown in order and witches will not throw a potion if the target has already the effect.")
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
                || !witch.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		LINGERING_CHANCE.applyIfAbsent(witch, lingeringChance);
		ATTACK_COOLDOWN.applyIfAbsent(witch, attackCooldown.getIntRandBetween(witch.getRandom()));
		ATTACK_RANGE.applyIfAbsent(witch, attackRange.getIntRandBetween(witch.getRandom()));
		INACCURACY.applyIfAbsent(witch, inaccuracy);
		APPRENTICE.applyIfAbsent(witch, witch.getRandom().nextDouble() < apprenticeChance);
		USE_SLOW_FALL.applyIfAbsent(witch, useSlowFalling);
		INVISIBILITY_HEALTH_THRESHOLD.applyIfAbsent(witch, invisibilityHealthThreshold);
		GoalHelper.removeGoal(witch.goalSelector, RangedAttackGoal.class);
		witch.goalSelector.addGoal(2, new WitchThrowPotionGoal(witch));
		//witch.targetSelector.addGoal(2, new WitchBuffAllyGoal<>(witch, Mob.class, true, (livingEntity -> livingEntity != null && !witch.hasActiveRaid() && livingEntity.getType() != EntityType.WITCH)));
    }

    @SubscribeEvent
    public void onWitchTick(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Witch witch)
                || !witch.getType().is(AFFECTED_ENTITY_TYPES)
                || !witch.isAlive()
                || witch.level().isClientSide
                || witch.isDrinkingPotion())
            return;

        if (WitchPotionThrowing.shouldUseSlowFalling() && witch.fallDistance > 7 && !witch.hasEffect(MobEffects.SLOW_FALLING)) {
            ItemStack slowFallingStack = MCUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0)));
            witch.getLookControl().setLookAt(witch.getX(), witch.getY(), witch.getZ());
            if (!witch.isSilent())
                witch.playSound(SoundEvents.WITCH_THROW, 1.0F, 0.8F + witch.getRandom().nextFloat() * 0.4F);
            witch.level().levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, witch.blockPosition(), PotionUtils.getColor(slowFallingStack));
            List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(slowFallingStack);
            for (MobEffectInstance mobEffect : mobEffects) {
                witch.addEffect(new MobEffectInstance(mobEffect));
            }
        }

        if (!witch.hasEffect(MobEffects.INVISIBILITY) && witch.onGround() && canUseInvisibility(witch) && witch.getHealth() < witch.getMaxHealth() * INVISIBILITY_HEALTH_THRESHOLD.get(witch)) {
            ThrownPotion thrownPotion = new ThrownPotion(witch.level(), witch);
            thrownPotion.setItem(MCUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(new MobEffectInstance(MobEffects.INVISIBILITY, 200))));
            thrownPotion.shoot(0, -1d, 0, 0.1f, 2f);
            witch.level().addFreshEntity(thrownPotion);

            //Try 5 times to find a random spot
            for (int i = 0; i < 5; i++) {
                Vec3 randomPos = DefaultRandomPos.getPos(witch, 16, 9);
                if (randomPos != null) {
                    witch.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, 1.1f);
                    break;
                }
            }

            ModNBTData.put(witch, INVISIBILITY_COOLDOWN, 20);
        }
    }

    public static boolean canUseInvisibility(Witch witch) {
        int cooldown = ModNBTData.get(witch, INVISIBILITY_COOLDOWN, Integer.class);
        if (--cooldown > 0) {
            ModNBTData.put(witch, INVISIBILITY_COOLDOWN, cooldown);
            return false;
        }
        return true;
    }

    public static boolean shouldUseSlowFalling() {
        return isEnabled(WitchPotionThrowing.class) && useSlowFalling;
    }
}