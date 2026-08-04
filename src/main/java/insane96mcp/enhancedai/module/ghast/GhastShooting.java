package insane96mcp.enhancedai.module.ghast;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ghast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;

@LoadFeature(module = EAIModules.GHAST, description = "Various changes to ghast shooting. Only ghast in enhancedai:ghast/change_shoot entity type tag are affected by this feature.")
public class GhastShooting extends Feature {
    public static final TagKey<EntityType<?>> CHANGE_GHAST_SHOOT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("ghast/change_shoot"));

    @Config(min = 1, max = 300, description = "How many ticks pass between shooting fireballs. Vanilla is 40")
    public static MinMaxConfig attackCooldown = new MinMaxConfig(40, 50);
    @Config(min = 1, max = 16, description = "How many fireballs ghast shoot in rapid succession. Vanilla is 1")
    public static MinMaxConfig fireballsShot = new MinMaxConfig(1, 3);
    @Config(min = 0d, max = 1d, description = "Chance for a Ghast to try and shoot the target even if can't see it. If enabled and the Ghast can't see the target, he will shoot 4 times as fast to breach.")
    public static Double shootWhenNotSeenChance = 0.3d;

    public static EAIData<Integer> ATTACK_COOLDOWN;
    public static EAIData<Integer> FIREBALLS_SHOT;
    public static EAIData<Boolean> SHOOT_WHEN_NOT_SEEN;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        ATTACK_COOLDOWN = EAIData.ofInt(this.createDataKey("attack_cooldown"));
        FIREBALLS_SHOT = EAIData.ofInt(this.createDataKey("fireballs_shot"));
        SHOOT_WHEN_NOT_SEEN = EAIData.ofBool(this.createDataKey("shoot_when_not_seen"));
    }

    //Low priority so other mods can set persistent data
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Ghast ghast)
                || !ghast.getType().is(CHANGE_GHAST_SHOOT))
            return;

        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        ghast.goalSelector.getAvailableGoals().forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof Ghast.GhastShootFireballGoal)
                goalsToRemove.add(prioritizedGoal.getGoal());
        });

        goalsToRemove.forEach(ghast.goalSelector::removeGoal);

        ghast.goalSelector.addGoal(4, new GhastShootFireballGoal(ghast));
        ATTACK_COOLDOWN.applyIfAbsent(ghast, attackCooldown.getIntRandBetween(ghast.getRandom()));
        FIREBALLS_SHOT.applyIfAbsent(ghast, fireballsShot.getIntRandBetween(ghast.getRandom()));
        SHOOT_WHEN_NOT_SEEN.applyIfAbsent(ghast, ghast.getRandom().nextDouble() < shootWhenNotSeenChance);
    }
}