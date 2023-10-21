package insane96mcp.enhancedai.modules.blaze;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Blaze Attack", description = "Make blazes fire faster/more fireballs")
@LoadFeature(module = Modules.Ids.BLAZE)
public class BlazeAttack extends Feature {
    public static final TagKey<EntityType<?>> CHANGE_BLAZE_ATTACK = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "change_blaze_attack"));

    public static final String TIME_BETWEEN_FIREBALLS = EnhancedAI.RESOURCE_PREFIX + "time_between_fireballs";
    public static final String FIREBALLS_SHOT = EnhancedAI.RESOURCE_PREFIX + "fireballs_shot";
    public static final String RECHARGE_TIME = EnhancedAI.RESOURCE_PREFIX + "recharge_time";
    public static final String CHARGE_TIME = EnhancedAI.RESOURCE_PREFIX + "charge_time";
    public static final String FIREBALLS_PER_SHOT = EnhancedAI.RESOURCE_PREFIX + "fireballs_per_shot";
    public static final String INACCURACY = EnhancedAI.RESOURCE_PREFIX + "inaccuracy";
    @Config(min = 1, max = 300)
    @Label(name = "Time Between Fireballs", description = "How many ticks pass between shooting fireballs. Vanilla is 6")
    public static MinMax timeBetweenFireballs = new MinMax(4, 10);
    @Config(min = 1, max = 64)
    @Label(name = "Fireballs shot", description = "How many fireballs blazes shoots. Vanilla is 3")
    public static MinMax fireballsShot = new MinMax(3, 8);
    @Config(min = 1, max = 600)
    @Label(name = "Recharge time", description = "Time (in ticks) taken by the blaze to recharge (before setting himself on fire). Vanilla is 100")
    public static MinMax rechargeTime = new MinMax(60, 100);
    @Config(min = 1, max = 600)
    @Label(name = "Charge time", description = "Time (in ticks) taken by the blaze to charge (while on fire before shooting fireballs). Vanilla is 60")
    public static MinMax chargeTime = new MinMax(30, 60);
    @Config(min = 1, max = 8)
    @Label(name = "Fireballs Per Shot", description = "How many fireballs are shot per shot. Vanilla is 1")
    public static MinMax fireballsPerShot = new MinMax(1, 2);
    @Config(min = -1, max = 32)
    @Label(name = "Inaccuracy", description = "The higher the more spread up shots will be. Setting both to -1 will use the vanilla behaviour (farther = more inaccuracy)")
    public static MinMax inaccuracy = new MinMax(2, 8);

    public BlazeAttack(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Blaze blaze)
                || !blaze.getType().is(CHANGE_BLAZE_ATTACK))
            return;

        CompoundTag persistentData = blaze.getPersistentData();

        int timeBetweenFireballs1 = NBTUtils.getIntOrPutDefault(persistentData, TIME_BETWEEN_FIREBALLS, timeBetweenFireballs.getIntRandBetween(blaze.getRandom()));
        int fireballsShot1 = NBTUtils.getIntOrPutDefault(persistentData, FIREBALLS_SHOT, fireballsShot.getIntRandBetween(blaze.getRandom()));
        int rechargeTime1 = NBTUtils.getIntOrPutDefault(persistentData, RECHARGE_TIME, rechargeTime.getIntRandBetween(blaze.getRandom()));
        int chargeTime1 = NBTUtils.getIntOrPutDefault(persistentData, CHARGE_TIME, chargeTime.getIntRandBetween(blaze.getRandom()));
        int fireballsPerShot1 = NBTUtils.getIntOrPutDefault(persistentData, FIREBALLS_PER_SHOT, fireballsPerShot.getIntRandBetween(blaze.getRandom()));
        int inaccuracy1 = NBTUtils.getIntOrPutDefault(persistentData, INACCURACY, inaccuracy.getIntRandBetween(blaze.getRandom()));

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        blaze.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Blaze.BlazeAttackGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(blaze.goalSelector::removeGoal);

        blaze.goalSelector.addGoal(4, new EABlazeAttackGoal(blaze)
                .setTimeBetweenFireballs(timeBetweenFireballs1)
                .setFireballShot(fireballsShot1)
                .setRechargeTime(rechargeTime1)
                .setChargeTime(chargeTime1)
                .setFireballsPerShot(fireballsPerShot1)
                .setInaccuracy(inaccuracy1));
    }
}