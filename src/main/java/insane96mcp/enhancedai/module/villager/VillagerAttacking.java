package insane96mcp.enhancedai.module.villager;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAIHurtByTargetGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.MeleeAttackGoalAccessor;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.VILLAGER, description = "Make villagers fight back. Use the enhancedai:villager/can_attack type tag to add more villagers, only works with entities that extend vanilla Villagers. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4.")
public class VillagerAttacking extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("villager/can_attack"));
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
			if (!mob.getType().is(AFFECTED_ENTITY_TYPES))
				return;
			GoalHelper.getGoal(mob.goalSelector, MeleeAttackGoal.class)
					.ifPresent(attackGoal -> ((MeleeAttackGoalAccessor) attackGoal).setSpeedModifier(speedMultiplier));
		});
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Villager villager)
                || !villager.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        villager.targetSelector.addGoal(1, (new EAIVillagerHurtByTargetGoal(villager)).setAlertOthers());
        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1f, false));

		FIGHTS_BACK_ENEMIES.applyIfAbsent(villager, villagersFightBackEnemies);
		ATTACK_BELOW_REPUTATION.applyIfAbsent(villager, minReputationFightBack);
		SPEED_MULTIPLIER.applyIfAbsent(villager, speedMultiplier);
    }

    public static class EAIVillagerHurtByTargetGoal extends EAIHurtByTargetGoal {

        Villager villager;

        public EAIVillagerHurtByTargetGoal(Villager villager, Class<?>... toIgnoreDamage) {
            super(villager, toIgnoreDamage);
            this.villager = villager;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse())
                return false;
            LivingEntity hurtVillager = this.mob.getLastHurtByMob();
            if (hurtVillager instanceof Player player)
                return villager.getPlayerReputation(player) <= ATTACK_BELOW_REPUTATION.get(this.villager);
            else if (hurtVillager instanceof Enemy)
                return FIGHTS_BACK_ENEMIES.get(this.villager);
            return true;
        }
    }
}