package insane96mcp.enhancedai.modules.blaze.feature;

import insane96mcp.enhancedai.config.IntMinMax;
import insane96mcp.enhancedai.modules.blaze.ai.EABlazeAttackGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Blaze Attack", description = "Make blazes fire faster/more fireballs")
public class BlazeAttack extends Feature {

    private final IntMinMax.Config timeBetweenFireballsConfig;
    private final IntMinMax.Config fireballsShotConfig;
    private final IntMinMax.Config rechargeTimeConfig;
    private final IntMinMax.Config chargeTimeConfig;
    private final IntMinMax.Config fireballsPerShotConfig;

    private final BlacklistConfig entityBlacklistConfig;

    public IntMinMax timeBetweenFireballs = new IntMinMax(2, 6);
    public IntMinMax fireballsShot = new IntMinMax(3, 8);
    public IntMinMax rechargeTime = new IntMinMax(60, 100);
    public IntMinMax chargeTime = new IntMinMax(30, 60);
    public IntMinMax fireballsPerShot = new IntMinMax(1, 3);

    public ArrayList<IdTagMatcher> entityBlacklist;
    public boolean entityBlacklistAsWhitelist;

    public BlazeAttack(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        this.timeBetweenFireballsConfig = new IntMinMax.Config(Config.builder, "Time Between Fireballs", "How many ticks pass between shooting fireballs. Vanilla is 6")
                .setMinMax(1, 300, this.timeBetweenFireballs)
                .build();
        this.fireballsShotConfig = new IntMinMax.Config(Config.builder, "Fireballs shot", "How many fireballs blazes shoot. Vanilla is 3")
                .setMinMax(1, 64, this.fireballsShot)
                .build();
        this.rechargeTimeConfig = new IntMinMax.Config(Config.builder, "Recharge time", "Time (in ticks) taken by the blaze to recharge (before setting himself on fire). Vanilla is 100")
                .setMinMax(1, 600, this.rechargeTime)
                .build();
        this.chargeTimeConfig = new IntMinMax.Config(Config.builder, "Charge time", "Time (in ticks) taken by the blaze to charge (while on fire before shooting fireballs). Vanilla is 60")
                .setMinMax(1, 600, this.chargeTime)
                .build();
        this.fireballsPerShotConfig = new IntMinMax.Config(Config.builder, "Fireballs Per Shot", "How many fireballs are shot per shot. Vanilla is 1")
                .setMinMax(1, 8, this.fireballsPerShot)
                .build();

        entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the new Blaze Attack AI", Collections.emptyList(), false);
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

        this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
        this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntity() instanceof Blaze blaze))
            return;

        //Check for black/whitelist
        boolean isInWhitelist = false;
        boolean isInBlacklist = false;
        for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
            if (blacklistEntry.matchesEntity(blaze)) {
                if (!this.entityBlacklistAsWhitelist)
                    isInBlacklist = true;
                else
                    isInWhitelist = true;
                break;
            }
        }
        if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
            return;

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        blaze.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Blaze.BlazeAttackGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(blaze.goalSelector::removeGoal);

        blaze.goalSelector.addGoal(4, new EABlazeAttackGoal(blaze)
                .setTimeBetweenFireballs(this.timeBetweenFireballs.getRandBetween(blaze.getRandom()))
                .setFireballShot(this.fireballsShot.getRandBetween(blaze.getRandom()))
                .setRechargeTime(this.rechargeTime.getRandBetween(blaze.getRandom()))
                .setChargeTime(this.chargeTime.getRandBetween(blaze.getRandom()))
                .setFireballsPerShot(this.fireballsPerShot.getRandBetween(blaze.getRandom())));
    }
}