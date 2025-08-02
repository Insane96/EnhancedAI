package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.MeleeAttackGoalAccessor;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.targeting.EANearestAttackableTarget;
import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.ANIMAL, description = "Make animals fight back or be scared by players. Use the entity type tag enhancedai:animal_scared_attack/can_be_neutral, enhancedai:animal_scared_attack/can_be_hostile, and enhancedai:animal_scared_attack/can_be_scared_by_players to add/remove animals.")
public class AnimalScaredAttack extends Feature {
    public static final TagKey<EntityType<?>> CAN_BE_NEUTRAL = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal_scared_attack/can_be_neutral"));
    public static final TagKey<EntityType<?>> CAN_BE_HOSTILE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal_scared_attack/can_be_hostile"));
    public static final TagKey<EntityType<?>> SCARED_BY_PLAYERS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal_scared_attack/can_be_scared_by_players"));

    public static EAIData<Boolean> NEUTRAL;
    public static EAIData<Boolean> HOSTILE;
    public static EAIData<Boolean> PLAYER_SCARED;
    public static EAIData<Double> ATTACK_MOVEMENT_SPEED_MODIFIER;
    public static EAIData<Integer> FLEE_DISTANCE_FAR;
    public static EAIData<Integer> FLEE_DISTANCE_NEAR;
    public static EAIData<Double> FLEE_SPEED_FAR;
    public static EAIData<Double> FLEE_SPEED_NEAR;

    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to be able to fight back instead of fleeing. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitations so use mods like Mobs Properties Randomness to change the damage. Base damage is 3")
    public static Double neutralChance = 0.35d;
    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to be hostile. Hostile animals are also neutral.")
    public static Double hostileChance = 0.10d;
    @Config(min = 0d, max = 4d, description = "Movement speed multiplier when aggroed.")
    public static Double speedModifier = 1.1d;
    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to be scared by players and run away. Fight back chance has priority over this")
    public static Double playersScaredChance = 0.25d;
    @Config(min = 0d, max = 32d, description = "Distance from a player that will make the entity run away. Higher values might impact performance")
    public static Integer fleeDistanceFar = 12;
    @Config(min = 0d, max = 32d, description = "Distance from a player that counts as near and will make the entity run away faster. Higher values might impact performance")
    public static Integer fleeDistanceNear = 7;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the animal avoids the player when it's within 'Flee Distance Far' blocks from them.")
    public static Double fleeSpeedFar = 1.1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the animal avoids the player and it's within 'Flee Distance Near' blocks from them.")
    public static Double fleeSpeedNear = 1.2d;
    @Config(min = 0d, max = 128d, description = "Animals' knockback attribute will be set to this value multiplied by their bounding box size (bigger mobs have higher knockback). 0 disables this and lets you customize knockback per mob with attribute modifiers.")
    public static Double knockback = 1.4d;
    @Config(description = "Animals' knockback attribute will be increased/decreased based on the side of the mob.")
    public static Boolean knockbackSizeBased = true;

    private static final double BASE_ATTACK_DAMAGE = 3d;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        NEUTRAL = EAIData.ofBool(this.createDataKey("neutral"), (mob, neutral) -> {
            if (!(mob instanceof PathfinderMob pathfinderMob))
                return;
            GoalHelper.removeGoal(mob.targetSelector, HurtByTargetGoal.class);
            GoalHelper.removeGoal(mob.goalSelector, AnimalMeleeAttackGoal.class);
            if (neutral) {
                mob.targetSelector.addGoal(1, (new HurtByTargetGoal(pathfinderMob)).setAlertOthers());
                mob.goalSelector.addGoal(1, new AnimalMeleeAttackGoal(pathfinderMob, ATTACK_MOVEMENT_SPEED_MODIFIER.get(mob), true));
            }
        });
        HOSTILE = EAIData.ofBool(this.createDataKey("hostile"), (mob, hostile) -> {
            GoalHelper.removeGoal(mob.targetSelector, AnimalNearestAttackableTargetGoal.class);
            mob.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(UUID.fromString("62e016b0-90d0-4e72-9d40-fffac566df20"));
            mob.getAttribute(EAAttributes.XRAY_FOLLOW_RANGE.get()).removeModifier(UUID.fromString("62e016b0-90d0-4e72-9d40-fffac566df20"));
            if (hostile) {
                NEUTRAL.apply(mob, true);
                MCUtils.applyModifier(mob, Attributes.FOLLOW_RANGE, UUID.fromString("62e016b0-90d0-4e72-9d40-fffac566df20"), "Reduced follow range for hostile Animals", -0.75d, AttributeModifier.Operation.MULTIPLY_BASE, true);
                MCUtils.applyModifier(mob, EAAttributes.XRAY_FOLLOW_RANGE.get(), UUID.fromString("62e016b0-90d0-4e72-9d40-fffac566df20"), "Reduced follow range for hostile Animals", -0.75d, AttributeModifier.Operation.MULTIPLY_BASE, true);
                mob.targetSelector.addGoal(2, new AnimalNearestAttackableTargetGoal<>(mob, Player.class, false, false));
                PLAYER_SCARED.apply(mob, false);
                ATTACK_MOVEMENT_SPEED_MODIFIER.changed(mob);
            }
        });
        PLAYER_SCARED = EAIData.ofBool(this.createDataKey("player_scared"), (mob, scared) -> {
            if (!(mob instanceof PathfinderMob pathfinderMob))
                return;
            GoalHelper.removeGoal(mob.goalSelector, AnimalAvoidPlayersGoal.class);
            if (scared)
                pathfinderMob.goalSelector.addGoal(1, new AnimalAvoidPlayersGoal(pathfinderMob, Player.class, FLEE_DISTANCE_FAR, FLEE_DISTANCE_NEAR, FLEE_SPEED_FAR, FLEE_SPEED_NEAR));
        });
        ATTACK_MOVEMENT_SPEED_MODIFIER = EAIData.ofDouble(this.createDataKey("attack_movement_speed_mod"), (mob, value) -> {
            GoalHelper.getGoal(mob.goalSelector, AnimalMeleeAttackGoal.class).ifPresent(animalMeleeAttackGoal -> ((MeleeAttackGoalAccessor) animalMeleeAttackGoal).setSpeedModifier(value));
        });
        FLEE_DISTANCE_FAR = EAIData.ofInt(this.createDataKey("flee_distance_far"));
        FLEE_DISTANCE_NEAR = EAIData.ofInt(this.createDataKey("flee_distance_near"));
        FLEE_SPEED_FAR = EAIData.ofDouble(this.createDataKey("flee_speed_far"));
        FLEE_SPEED_NEAR = EAIData.ofDouble(this.createDataKey("flee_speed_near"));
    }

    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (event.has(entityType, Attributes.ATTACK_DAMAGE))
                continue;

            event.add(entityType, Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE);
        }
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal))
            return;

        if (knockback > 0d) {
            double baseSize = 1.053d; // Sheep square meters size
            double actualKnockback = knockback;
            if (knockbackSizeBased)
                actualKnockback = (animal.getBbWidth() * animal.getBbWidth() * animal.getBbHeight()) * knockback / baseSize;
            AttributeInstance kbAttribute = animal.getAttribute(Attributes.ATTACK_KNOCKBACK);
            if (kbAttribute != null)
                kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", actualKnockback, AttributeModifier.Operation.ADDITION));
        }

        ATTACK_MOVEMENT_SPEED_MODIFIER.applyIfAbsent(animal, speedModifier);
        NEUTRAL.applyIfAbsent(animal, animal.getType().is(CAN_BE_NEUTRAL) && animal.getRandom().nextDouble() < neutralChance);
        HOSTILE.applyIfAbsent(animal, animal.getType().is(CAN_BE_HOSTILE) && animal.getRandom().nextDouble() < hostileChance);
        PLAYER_SCARED.applyIfAbsent(animal, !HOSTILE.get(animal) && animal.getType().is(SCARED_BY_PLAYERS) && animal.getRandom().nextDouble() < playersScaredChance);
        FLEE_DISTANCE_FAR.applyIfAbsent(animal, fleeDistanceFar);
        FLEE_DISTANCE_NEAR.applyIfAbsent(animal, fleeDistanceNear);
        FLEE_SPEED_FAR.applyIfAbsent(animal, fleeSpeedFar);
        FLEE_SPEED_NEAR.applyIfAbsent(animal, fleeSpeedNear);
    }

    public static class AnimalMeleeAttackGoal extends MeleeAttackGoal {
        public AnimalMeleeAttackGoal(PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            if (this.mob instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null)
                return false;
            return !this.mob.isBaby() && super.canUse();
        }
    }

    public static class AnimalNearestAttackableTargetGoal<T extends LivingEntity> extends EANearestAttackableTarget<T> {

        public AnimalNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean mustSee, boolean mustReach) {
            super(goalOwnerIn, targetClassIn, mustSee, mustReach, TargetingConditions.forCombat());
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.mob.isBaby();
        }
    }

    public static class AnimalAvoidPlayersGoal extends EAAvoidEntityGoal<Player> {
        public AnimalAvoidPlayersGoal(PathfinderMob entity, Class<Player> classToAvoidIn, EAIData<Integer> avoidDistance, EAIData<Integer> avoidDistanceNear, EAIData<Double> farSpeed, EAIData<Double> nearSpeed) {
            super(entity, classToAvoidIn, avoidDistance, avoidDistanceNear, farSpeed, nearSpeed);
        }

        @Override
        public boolean canUse() {
            if (this.goalOwner instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null)
                return false;
            return super.canUse();
        }
    }
}