package insane96mcp.enhancedai.modules.ghast.feature;

import insane96mcp.enhancedai.modules.ghast.ai.GhastShootFireballGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Ghast Shoot", description = "Various changes to ghast shooting.")
public class GhastShoot extends Feature {

    private final MinMax.Config attackCooldownConfig;
    private final MinMax.Config fireballsShotConfig;
    private final ForgeConfigSpec.DoubleValue shootWhenNotSeenConfig;

    private final Blacklist.Config entityBlacklistConfig;

    public MinMax attackCooldown = new MinMax(40, 50);
    public MinMax fireballsShot = new MinMax(1, 3);
    public double shootWhenNotSeen = 0.3d;

    public Blacklist entityBlacklist;

    public GhastShoot(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        this.attackCooldownConfig = new MinMax.Config(Config.builder, "Attack Cooldown", "How many ticks pass between shooting fireballs. Vanilla is 40")
                .setMinMax(1, 300, this.attackCooldown)
                .build();
        this.fireballsShotConfig = new MinMax.Config(Config.builder, "Fireballs shot", "How many fireballs ghast shoot in rapid succession. Vanilla is 1")
                .setMinMax(1, 16, this.fireballsShot)
                .build();
        this.shootWhenNotSeenConfig = Config.builder
                .comment("Chance for a Ghast to try and shoot the target even if can't see it. If enabled and the Ghast can't see the target, he will shoot 4 times as fast to breach.")
                .defineInRange("Shoot when not seen", this.shootWhenNotSeen, 0d, 1d);

        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the new Ghast Fireballing AI")
                .setDefaultList(Collections.emptyList())
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.attackCooldown = this.attackCooldownConfig.get();
        this.fireballsShot = this.fireballsShotConfig.get();
        this.shootWhenNotSeen = this.shootWhenNotSeenConfig.get();

        this.entityBlacklist = this.entityBlacklistConfig.get();
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled())
            return;

        if (!(event.getEntity() instanceof Ghast ghast))
            return;

        if (this.entityBlacklist.isEntityBlackOrNotWhitelist(ghast))
            return;

        CompoundTag persistentData = ghast.getPersistentData();

        int attackCooldown = NBTUtils.getIntOrPutDefault(persistentData, Strings.Tags.Ghast.ATTACK_COOLDOWN, this.attackCooldown.getIntRandBetween(ghast.getRandom()));
        int fireballsShot = NBTUtils.getIntOrPutDefault(persistentData, Strings.Tags.Ghast.FIREBALLS_SHOT, this.fireballsShot.getIntRandBetween(ghast.getRandom()));
        boolean shootWhenNotSeen = NBTUtils.getBooleanOrPutDefault(persistentData, Strings.Tags.Ghast.SHOOT_WHEN_NOT_SEEN, ghast.getRandom().nextDouble() < this.shootWhenNotSeen);

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        ghast.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Ghast.GhastShootFireballGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(ghast.goalSelector::removeGoal);

        ghast.goalSelector.addGoal(4, new GhastShootFireballGoal(ghast)
                .setAttackCooldown(attackCooldown)
                .setFireballsToShot(fireballsShot)
                .setIgnoreLineOfSight(shootWhenNotSeen));
    }
}