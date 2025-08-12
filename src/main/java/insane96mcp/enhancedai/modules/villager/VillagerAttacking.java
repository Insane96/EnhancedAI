package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.MeleeAttackGoalAccessor;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

@LoadFeature(module = Modules.Ids.VILLAGER, description = "Make villagers fight back. Use the enhancedai:villager_attacking/can_attack type tag to add more villagers, only works with entities that extend vanilla Villagers. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4.")
public class VillagerAttacking extends Feature {
    public static final TagKey<EntityType<?>> VILLAGERS_CAN_ATTACK = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager_attacking/can_attack"));
    @Config(description = "If true, villagers will attack back monsters")
    public static Boolean villagersFightBackEnemies = false;
    @Config(description = "Villagers will only attack players that have below this reputation (like Iron Golems by default). https://minecraft.wiki/w/Villager#Gossiping")
    public static Integer minReputationFightBack = -100;
    @Config(min = 0d, max = 4d, description = "Movement speed multiplier when attacking")
    public static Double speedMultiplier = 0.4d;

	public static EAIData<Boolean> FIGHTS_BACK_ENEMIES;
	public static EAIData<Integer> ATTACK_BELOW_REPUTATION;
	public static EAIData<Double> SPEED_MULTIPLIER;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		FIGHTS_BACK_ENEMIES = EAIData.ofBool(this.createDataKey("fights_back_enemies"));
		ATTACK_BELOW_REPUTATION = EAIData.ofInt(this.createDataKey("attack_below_reputation"));
		SPEED_MULTIPLIER = EAIData.ofDouble(this.createDataKey("speed_multiplier"), (mob, speedMultiplier) -> {
			if (!mob.getType().is(VILLAGERS_CAN_ATTACK))
				return;
			Optional<MeleeAttackGoal> meleeAttackGoal = GoalHelper.getGoal(mob.goalSelector, MeleeAttackGoal.class);
			meleeAttackGoal.ifPresent(attackGoal -> ((MeleeAttackGoalAccessor) attackGoal).setSpeedModifier(speedMultiplier));
		});
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Villager villager)
                || !villager.getType().is(VILLAGERS_CAN_ATTACK))
            return;

        villager.targetSelector.addGoal(1, (new EAVillagerHurtByTargetGoal(villager)).setAlertOthers());
        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1f, false));

		FIGHTS_BACK_ENEMIES.applyIfAbsent(villager, villagersFightBackEnemies);
		ATTACK_BELOW_REPUTATION.applyIfAbsent(villager, minReputationFightBack);
		SPEED_MULTIPLIER.applyIfAbsent(villager, speedMultiplier);
    }
}