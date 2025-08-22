package insane96mcp.enhancedai.modules.snowgolem.shooting;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SNOW_GOLEM, description = "Enhance and customize shooting. Only entity types in enhancedai:snow_golem/better_shooting tag are affected by this feature.")
public class SnowGolemsShooting extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golem/better_shooting"));
    @Config(min = 1, max = 64, description = "The max range from where a snow golem will shoot the target")
    public static MinMax range = new MinMax(24, 32);
    @Config(min = 0, description = "The ticks cooldown before shooting. Vanilla is random between 20 and 40")
    public static MinMax cooldown = new MinMax(15, 30);
    @Config(min = 0d, max = 30d, description = "How much inaccuracy does the snowball fired by snow golems have. Vanilla is 12.")
    public static MinMax inaccuracy = new MinMax(1, 3);
    @Config(min = 0d, max = 1d, description = "Chance to be able to strafe while shooting. Vanilla golems don't strafe")
    public static Double strafeChance = 0d;

    public static EAIData<Integer> SHOOTING_RANGE;
    public static EAIData<Double> INACCURACY;
    public static EAIData<Integer> SHOOTING_COOLDOWN;
    public static EAIData<Boolean> STRAFE;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        SHOOTING_RANGE = EAIData.ofInt(this.createDataKey("shooting_range"));
        INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
        SHOOTING_COOLDOWN = EAIData.ofInt(this.createDataKey("shooting_cooldown"));
        STRAFE = EAIData.ofBool(this.createDataKey("strafe"));
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof SnowGolem snowGolem)
                || !snowGolem.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		GoalHelper.removeGoal(snowGolem.goalSelector, RangedAttackGoal.class);
        snowGolem.goalSelector.addGoal(1, new EAIRangedSnowGolemAttackGoal(snowGolem, 1f, SHOOTING_COOLDOWN, INACCURACY, SHOOTING_RANGE, STRAFE));
        SHOOTING_RANGE.applyIfAbsent(snowGolem, range.getIntRandBetween(snowGolem.getRandom()));
        INACCURACY.applyIfAbsent(snowGolem, inaccuracy.getRandBetween(snowGolem.getRandom()));
        SHOOTING_COOLDOWN.applyIfAbsent(snowGolem, cooldown.getIntRandBetween(snowGolem.getRandom()));
        STRAFE.applyIfAbsent(snowGolem, snowGolem.getRandom().nextDouble() < strafeChance);
    }
}