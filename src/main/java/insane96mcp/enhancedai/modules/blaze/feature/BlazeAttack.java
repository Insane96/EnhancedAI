package insane96mcp.enhancedai.modules.blaze.feature;

import insane96mcp.enhancedai.modules.blaze.ai.EABlazeAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Blaze Attack", description = "Make blazes fire faster/more fireballs")
public class BlazeAttack extends Feature {

    private final MinMax.Config timeBetweenFireballsConfig;
    private final MinMax.Config fireballsShotConfig;
    private final MinMax.Config rechargeTimeConfig;
    private final MinMax.Config chargeTimeConfig;
    private final MinMax.Config fireballsPerShotConfig;
    private final MinMax.Config inaccuracyConfig;

    private final Blacklist.Config entityBlacklistConfig;

    public MinMax timeBetweenFireballs = new MinMax(3, 6);
    public MinMax fireballsShot = new MinMax(3, 8);
    public MinMax rechargeTime = new MinMax(60, 100);
    public MinMax chargeTime = new MinMax(30, 60);
    public MinMax fireballsPerShot = new MinMax(1, 2);
    public MinMax inaccuracy = new MinMax(2, 14);

    public Blacklist entityBlacklist;

    public BlazeAttack(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        this.timeBetweenFireballsConfig = new MinMax.Config(Config.builder, "Time Between Fireballs", "How many ticks pass between shooting fireballs. Vanilla is 6")
                .setMinMax(1, 300, this.timeBetweenFireballs)
                .build();
        this.fireballsShotConfig = new MinMax.Config(Config.builder, "Fireballs shot", "How many fireballs blazes shoot. Vanilla is 3")
                .setMinMax(1, 64, this.fireballsShot)
                .build();
        this.rechargeTimeConfig = new MinMax.Config(Config.builder, "Recharge time", "Time (in ticks) taken by the blaze to recharge (before setting himself on fire). Vanilla is 100")
                .setMinMax(1, 600, this.rechargeTime)
                .build();
        this.chargeTimeConfig = new MinMax.Config(Config.builder, "Charge time", "Time (in ticks) taken by the blaze to charge (while on fire before shooting fireballs). Vanilla is 60")
                .setMinMax(1, 600, this.chargeTime)
                .build();
        this.fireballsPerShotConfig = new MinMax.Config(Config.builder, "Fireballs Per Shot", "How many fireballs are shot per shot. Vanilla is 1")
                .setMinMax(1, 8, this.fireballsPerShot)
                .build();
        this.inaccuracyConfig = new MinMax.Config(Config.builder, "Inaccuracy", "The higher the more spread up shots will be. Setting both to -1 will use the vanilla behaviour")
                .setMinMax(-1, 32, this.inaccuracy)
                .build();

        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the new Blaze Attack AI")
                .setDefaultList(Collections.emptyList())
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.timeBetweenFireballs = this.timeBetweenFireballsConfig.get();
        this.fireballsShot = this.fireballsShotConfig.get();
        this.rechargeTime = this.rechargeTimeConfig.get();
        this.chargeTime = this.chargeTimeConfig.get();
        this.fireballsPerShot = this.fireballsPerShotConfig.get();
        this.inaccuracy = this.inaccuracyConfig.get();

        this.entityBlacklist = this.entityBlacklistConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntity() instanceof Blaze blaze))
            return;

        if (this.entityBlacklist.isEntityBlackOrNotWhitelist(blaze))
            return;

        CompoundTag persistentData = blaze.getPersistentData();

        int timeBetweenFireballs = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Blaze.TIME_BETWEEN_FIREBALLS, this.timeBetweenFireballs.getIntRandBetween(blaze.getRandom()));
        int fireballsShot = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Blaze.FIREBALLS_SHOT, this.fireballsShot.getIntRandBetween(blaze.getRandom()));
        int rechargeTime = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Blaze.RECHARGE_TIME, this.rechargeTime.getIntRandBetween(blaze.getRandom()));
        int chargeTime = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Blaze.CHARGE_TIME, this.chargeTime.getIntRandBetween(blaze.getRandom()));
        int fireballsPerShot = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Blaze.FIREBALLS_PER_SHOT, this.fireballsPerShot.getIntRandBetween(blaze.getRandom()));
        int inaccuracy = NBTUtils.getIntOrPutDefault(persistentData, EAStrings.Tags.Blaze.INACCURACY, this.inaccuracy.getIntRandBetween(blaze.getRandom()));

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        blaze.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Blaze.BlazeAttackGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(blaze.goalSelector::removeGoal);

        blaze.goalSelector.addGoal(4, new EABlazeAttackGoal(blaze)
                .setTimeBetweenFireballs(timeBetweenFireballs)
                .setFireballShot(fireballsShot)
                .setRechargeTime(rechargeTime)
                .setChargeTime(chargeTime)
                .setFireballsPerShot(fireballsPerShot)
                .setInaccuracy(inaccuracy));
    }
}