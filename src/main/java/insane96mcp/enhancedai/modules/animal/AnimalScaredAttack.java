package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.targeting.EANearestAttackableTarget;
import insane96mcp.enhancedai.setup.EAAttributes;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
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

@LoadFeature(module = Modules.Ids.ANIMAL, description = "Make animals fight back or be scared by players. Use the entity type tag enhancedai:can_be_neutral, enhancedai:can_be_hostile, and enhancedai:can_be_scared_by_players to add/remove animals.")
public class AnimalScaredAttack extends Feature {
    public static final TagKey<EntityType<?>> CAN_BE_NEUTRAL = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(EnhancedAI.MOD_ID, "can_be_neutral"));
    public static final TagKey<EntityType<?>> CAN_BE_HOSTILE = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(EnhancedAI.MOD_ID, "can_be_hostile"));
    public static final TagKey<EntityType<?>> SCARED_BY_PLAYERS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(EnhancedAI.MOD_ID, "can_be_scared_by_players"));
    public static final String NEUTRAL = EnhancedAI.RESOURCE_PREFIX + "neutral";
    public static final String HOSTILE = EnhancedAI.RESOURCE_PREFIX + "hostile";
    public static final String PLAYER_SCARED = EnhancedAI.RESOURCE_PREFIX + "player_scared";

    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to be able to fight back instead of fleeing. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitations so use mods like Mobs Properties Randomness to change the damage. Base damage is 3")
    public static Double neutralChance = 0.35d;
    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to be hostile")
    public static Double hostileChance = 0.10d;
    @Config(min = 0d, max = 1d, description = "Animals have this percentage chance to be scared by players and run away. Fight back chance has priority over this")
    public static Double playersScaredChance = 0.25d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the animal avoids the player and it's within 8 blocks from him.")
    public static Double fleeSpeedNear = 1.1d;
    @Config(min = 0d, max = 4d, description = "Speed multiplier when the animal avoids the player and it's farther than 16 blocks from him.")
    public static Double fleeSpeedFar = 1d;
    @Config(min = 0d, max = 4d, description = "Movement speed multiplier when aggroed.")
    public static Double speedMultiplier = 1.1d;
    @Config(min = 0d, max = 128d, description = "Animals' knockback attribute will be set to this value.")
    public static Double knockback = 1.5d;
    @Config(description = "Animals' knockback attribute will be increased/decreased based on the side of the mob.")
    public static Boolean knockbackSizeBased = true;

    private static final double BASE_ATTACK_DAMAGE = 3d;

    public AnimalScaredAttack(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
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

        CompoundTag persistentData = animal.getPersistentData();

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefaultLegacy(persistentData, EATags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, speedMultiplier);
        boolean neutral = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, NEUTRAL, animal.getType().is(CAN_BE_NEUTRAL) && animal.getRandom().nextDouble() < neutralChance);
        boolean hostile = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, HOSTILE, animal.getType().is(CAN_BE_HOSTILE) && !animal.isBaby() && animal.getRandom().nextDouble() < hostileChance);
        boolean playerScared = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, PLAYER_SCARED, !neutral && animal.getType().is(SCARED_BY_PLAYERS) && animal.getRandom().nextDouble() < playersScaredChance);

        if (neutral || hostile) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new AnimalMeleeAttackGoal(animal, movementSpeedMultiplier, true));
            animal.goalSelector.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof PanicGoal);
            if (knockback > 0d) {
                double baseSize = 1.053d; // Sheep square meters size
                double actualKnockback = knockback;
                if (knockbackSizeBased)
                    actualKnockback = (animal.getBbWidth() * animal.getBbWidth() * animal.getBbHeight()) * knockback / baseSize;
                AttributeInstance kbAttribute = animal.getAttribute(Attributes.ATTACK_KNOCKBACK);
                if (kbAttribute != null)
                    kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", actualKnockback, AttributeModifier.Operation.ADDITION));
            }
            if (hostile) {
                animal.targetSelector.addGoal(2, new EANearestAttackableTarget<>(animal, Player.class, false, false, TargetingConditions.forCombat()));
                MCUtils.applyModifier(animal, Attributes.FOLLOW_RANGE, UUID.fromString("62e016b0-90d0-4e72-9d40-fffac566df20"), "Reduced follow range for hostile Animals", -0.8d, AttributeModifier.Operation.MULTIPLY_BASE, true);
                MCUtils.applyModifier(animal, EAAttributes.XRAY_FOLLOW_RANGE.get(), UUID.fromString("62e016b0-90d0-4e72-9d40-fffac566df20"), "Reduced follow range for hostile Animals", -0.8d, AttributeModifier.Operation.MULTIPLY_BASE, true);
            }
        }
        else if (playerScared) {
            AnimalAvoidPlayersGoal avoidEntityGoal = new AnimalAvoidPlayersGoal(animal, Player.class, (float) 16, (float) 8, fleeSpeedNear, fleeSpeedFar);
            animal.goalSelector.addGoal(1, avoidEntityGoal);
        }
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

    public static class AnimalAvoidPlayersGoal extends EAAvoidEntityGoal<Player> {
        public AnimalAvoidPlayersGoal(PathfinderMob entity, Class<Player> classToAvoidIn, float avoidDistance, float avoidDistanceNear, double nearSpeed, double farSpeed) {
            super(entity, classToAvoidIn, avoidDistance, avoidDistanceNear, nearSpeed, farSpeed);
        }

        @Override
        public boolean canUse() {
            if (this.goalOwner instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null)
                return false;
            return super.canUse();
        }
    }
}