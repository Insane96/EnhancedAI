package insane96mcp.enhancedai.modules.ghast.feature;

import insane96mcp.enhancedai.modules.ghast.ai.GhastShootFireballGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Ghast Shoot", description = "Various changes to ghast shooting.")
public class GhastShoot extends Feature {

    private final Blacklist.Config entityBlacklistConfig;

    public Blacklist entityBlacklist;

    public GhastShoot(Module module) {
        super(Config.builder, module);
        this.pushConfig(Config.builder);
        entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Throwing Web AI")
                .setDefaultList(Collections.emptyList())
                .setIsDefaultWhitelist(false)
                .build();
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
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

        int attackCooldown = 10;
        int fireballsShot = 1;

        if (persistentData.contains(Strings.Tags.Ghast.ATTACK_COOLDOWN)) {
            attackCooldown = persistentData.getInt(Strings.Tags.Ghast.ATTACK_COOLDOWN);
            fireballsShot = persistentData.getInt(Strings.Tags.Ghast.FIREBALLS_SHOT);
        }
        else {
            persistentData.putInt(Strings.Tags.Ghast.ATTACK_COOLDOWN, attackCooldown);
            persistentData.putInt(Strings.Tags.Ghast.FIREBALLS_SHOT, fireballsShot);
        }

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        ghast.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Ghast.GhastShootFireballGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(ghast.goalSelector::removeGoal);

        ghast.goalSelector.addGoal(4, new GhastShootFireballGoal(ghast)
                .setAttackCooldown(attackCooldown)
                .setFireballShot(fireballsShot));
    }
}