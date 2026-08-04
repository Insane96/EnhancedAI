package insane96mcp.enhancedai.module.blaze;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.BLAZE, description = "Make blazes fire faster/more fireballs. Only mobs in enhancedai:blaze_attack/change_attack entity type tag are affected by this feature.")
public class BlazeAttack extends Feature {
    public static final TagKey<EntityType<?>> CHANGE_ATTACK = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("blaze_attack/change_attack"));

    @Config(min = 1, max = 300, description = "How many ticks pass between shooting fireballs. Vanilla is 6")
    public static MinMaxConfig timeBetweenFireballs = new MinMaxConfig(4, 10);
    @Config(min = 1, max = 64, description = "How many fireballs blazes shoots. Vanilla is 3")
    public static MinMaxConfig fireballsShot = new MinMaxConfig(2, 6);
    @Config(min = 1, max = 600, description = "Time (in ticks) taken by the blaze to recharge (before setting himself on fire). Vanilla is 100")
    public static MinMaxConfig rechargeTime = new MinMaxConfig(60, 120);
    @Config(min = 1, max = 600, description = "Time (in ticks) taken by the blaze to charge (while on fire before shooting fireballs). Vanilla is 60")
    public static MinMaxConfig chargeTime = new MinMaxConfig(30, 80);
    @Config(min = 1, max = 8, description = "How many fireballs are shot per shot. Vanilla is 1")
    public static MinMaxConfig fireballsPerShot = new MinMaxConfig(1, 2);
    @Config(min = -1, max = 32, description = "The higher the more spread up shots will be. Setting both to -1 will use the vanilla behaviour (farther = more inaccuracy)")
    public static MinMaxConfig inaccuracy = new MinMaxConfig(1, 3);

    public static EAIData<Integer> TIME_BETWEEN_FIREBALLS;
    public static EAIData<Integer> FIREBALLS_SHOT;
    public static EAIData<Integer> RECHARGE_TIME;
    public static EAIData<Integer> CHARGE_TIME;
    public static EAIData<Integer> FIREBALLS_PER_SHOT;
    public static EAIData<Integer> INACCURACY;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        TIME_BETWEEN_FIREBALLS = EAIData.ofInt(this.createDataKey("time_between_fireballs"));
        FIREBALLS_SHOT = EAIData.ofInt(this.createDataKey("fireballs_shot"));
        RECHARGE_TIME = EAIData.ofInt(this.createDataKey("recharge_time"));
        CHARGE_TIME = EAIData.ofInt(this.createDataKey("charge_time"));
        FIREBALLS_PER_SHOT = EAIData.ofInt(this.createDataKey("fireballs_per_shot"));
        INACCURACY = EAIData.ofInt(this.createDataKey("inaccuracy"));
    }

    //Low priority so other mods can set persistent data
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Blaze blaze)
                || !blaze.getType().is(CHANGE_ATTACK))
            return;

        GoalHelper.removeGoal(blaze.goalSelector, Blaze.BlazeAttackGoal.class);
        blaze.goalSelector.addGoal(4, new EAIBlazeAttackGoal(blaze));

        TIME_BETWEEN_FIREBALLS.applyIfAbsent(blaze, timeBetweenFireballs.getIntRandBetween(blaze.getRandom()));
        FIREBALLS_SHOT.applyIfAbsent(blaze, fireballsShot.getIntRandBetween(blaze.getRandom()));
        RECHARGE_TIME.applyIfAbsent(blaze, rechargeTime.getIntRandBetween(blaze.getRandom()));
        CHARGE_TIME.applyIfAbsent(blaze, chargeTime.getIntRandBetween(blaze.getRandom()));
        FIREBALLS_PER_SHOT.applyIfAbsent(blaze, fireballsPerShot.getIntRandBetween(blaze.getRandom()));
        INACCURACY.applyIfAbsent(blaze, inaccuracy.getIntRandBetween(blaze.getRandom()));
    }
}