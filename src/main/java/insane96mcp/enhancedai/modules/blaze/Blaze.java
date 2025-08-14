package insane96mcp.enhancedai.modules.blaze;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@LoadFeature(module = Modules.Ids.BLAZE, description = "Make blazes fire faster/more fireballs. Only mobs in enhancedai:blaze/change_attack entity type tag are affected by this feature.")
public class Blaze extends Feature {
    public static final TagKey<EntityType<?>> CHANGE_BLAZE_ATTACK = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("blaze/change_attack"));

    @Config(min = 1, max = 300, description = "How many ticks pass between shooting fireballs. Vanilla is 6")
    public static MinMax timeBetweenFireballs = new MinMax(4, 10);
    @Config(min = 1, max = 64, description = "How many fireballs blazes shoots. Vanilla is 3")
    public static MinMax fireballsShot = new MinMax(2, 6);
    @Config(min = 1, max = 600, description = "Time (in ticks) taken by the blaze to recharge (before setting himself on fire). Vanilla is 100")
    public static MinMax rechargeTime = new MinMax(60, 120);
    @Config(min = 1, max = 600, description = "Time (in ticks) taken by the blaze to charge (while on fire before shooting fireballs). Vanilla is 60")
    public static MinMax chargeTime = new MinMax(30, 80);
    @Config(min = 1, max = 8, description = "How many fireballs are shot per shot. Vanilla is 1")
    public static MinMax fireballsPerShot = new MinMax(1, 2);
    @Config(min = -1, max = 32, description = "The higher the more spread up shots will be. Setting both to -1 will use the vanilla behaviour (farther = more inaccuracy)")
    public static MinMax inaccuracy = new MinMax(1, 3);

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

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof net.minecraft.world.entity.monster.Blaze blaze)
                || !blaze.getType().is(CHANGE_BLAZE_ATTACK))
            return;

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        blaze.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof net.minecraft.world.entity.monster.Blaze.BlazeAttackGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(blaze.goalSelector::removeGoal);

        blaze.goalSelector.addGoal(4, new EAIBlazeAttackGoal(blaze));
        TIME_BETWEEN_FIREBALLS.applyIfAbsent(blaze, timeBetweenFireballs.getIntRandBetween(blaze.getRandom()));
        FIREBALLS_SHOT.applyIfAbsent(blaze, fireballsShot.getIntRandBetween(blaze.getRandom()));
        RECHARGE_TIME.applyIfAbsent(blaze, rechargeTime.getIntRandBetween(blaze.getRandom()));
        CHARGE_TIME.applyIfAbsent(blaze, chargeTime.getIntRandBetween(blaze.getRandom()));
        FIREBALLS_PER_SHOT.applyIfAbsent(blaze, fireballsPerShot.getIntRandBetween(blaze.getRandom()));
        INACCURACY.applyIfAbsent(blaze, inaccuracy.getIntRandBetween(blaze.getRandom()));
    }
}