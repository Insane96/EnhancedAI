package insane96mcp.enhancedai.modules.ghast;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Ghast Shoot", description = "Various changes to ghast shooting.")
@LoadFeature(module = Modules.Ids.GHAST)
public class GhastShoot extends Feature {
    public static final String ATTACK_COOLDOWN = EnhancedAI.RESOURCE_PREFIX + "attack_cooldown";
    public static final String FIREBALLS_SHOT = EnhancedAI.RESOURCE_PREFIX + "fireballs_shot";
    public static final String SHOOT_WHEN_NOT_SEEN = EnhancedAI.RESOURCE_PREFIX + "shoot_when_not_seen";
    @Config(min = 1, max = 300)
    @Label(name = "Attack Cooldown", description = "How many ticks pass between shooting fireballs. Vanilla is 40")
    public static MinMax attackCooldown = new MinMax(40, 50);
    @Config(min = 1, max = 16)
    @Label(name = "Fireballs shot", description = "How many fireballs ghast shoot in rapid succession. Vanilla is 1")
    public static MinMax fireballsShot = new MinMax(1, 3);
    @Config(min = 0d, max = 1d)
    @Label(name = "Shoot when not seen Chance", description = "Chance for a Ghast to try and shoot the target even if can't see it. If enabled and the Ghast can't see the target, he will shoot 4 times as fast to breach.")
    public static Double shootWhenNotSeenChance = 0.3d;

    @Config
    @Label(name = "Entity Blacklist", description = "Entities that will not be affected by this feature.")
    public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

    public GhastShoot(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Ghast ghast)
                || entityBlacklist.isEntityBlackOrNotWhitelist(ghast))
            return;

        CompoundTag persistentData = ghast.getPersistentData();

        int attackCooldown1 = NBTUtils.getIntOrPutDefault(persistentData, ATTACK_COOLDOWN, attackCooldown.getIntRandBetween(ghast.getRandom()));
        int fireballsShot1 = NBTUtils.getIntOrPutDefault(persistentData, FIREBALLS_SHOT, fireballsShot.getIntRandBetween(ghast.getRandom()));
        boolean shootWhenNotSeen = NBTUtils.getBooleanOrPutDefault(persistentData, SHOOT_WHEN_NOT_SEEN, ghast.getRandom().nextDouble() < shootWhenNotSeenChance);

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        ghast.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Ghast.GhastShootFireballGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(ghast.goalSelector::removeGoal);

        ghast.goalSelector.addGoal(4, new GhastShootFireballGoal(ghast)
                .setAttackCooldown(attackCooldown1)
                .setFireballsToShot(fireballsShot1)
                .setIgnoreLineOfSight(shootWhenNotSeen));
    }
}