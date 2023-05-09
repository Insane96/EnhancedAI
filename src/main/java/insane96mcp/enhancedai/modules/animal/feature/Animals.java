package insane96mcp.enhancedai.modules.animal.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.base.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Animals", description = "Make animals fight back or group flee when attacked")
@LoadFeature(module = Modules.Ids.ANIMAL)
public class Animals extends Feature {

    public static final String CAN_ATTACK_BACK = EnhancedAI.RESOURCE_PREFIX + "can_attack_back";
    public static final String PLAYER_SCARED = EnhancedAI.RESOURCE_PREFIX + "player_scared";
    public static final String NOT_TEMPTED = EnhancedAI.RESOURCE_PREFIX + "not_tempted";

    @Config
    @Label(name = "Group Flee", description = "If true, when an animal is attacked, all the animals around will flee.")
    public static Boolean groupFlee = true;
    @Config
    @Label(name = "Flee Range", description = "If Group Flee is enabled, this is the range where the animals will flee.")
    public static Integer groupFleeRange = 16;
    @Config
    @Label(name = "Fight back chance", description = "Animals have this percentage chance to be able to fight back instead of fleeing. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 3.")
    public static Double fightBackChance = 0.2d;
    @Config
    @Label(name = "Players Scared chance", description = "Animals have this percentage chance to be scared by players and run away. Fight back chance has priority over this.")
    public static Double playersScaredChance = 0.4d;
    @Config
    @Label(name = "Not tempted chance", description = "Animals have this percentage chance to not be temped by food.")
    public static Double notTemptedChance = 0.5d;
    @Config(min = 0d, max = 4d)
    @Label(name = "Movement Speed Multiplier", description = "Movement speed multiplier when aggroed.")
    public static Double speedMultiplier = 1.35d;
    @Config(min = 0d, max = 128d)
    @Label(name = "Knockback", description = "Animals' knockback attribute will be set to this value.")
    public static Double knockback = 1.5d;
    @Config
    @Label(name = "Entity Blacklist", description = "Entities that shouldn't be affected by this feature")
    public static Blacklist entityBlacklist = new Blacklist(List.of(
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:llama"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:trader_llama"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:bee"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:wolf"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:bear"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:panda")
    ), false);

    private static final double BASE_ATTACK_DAMAGE = 3d;

    public Animals(Module module, boolean enabledByDefault, boolean canBeDisabled) {
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
                || !(event.getEntity() instanceof Animal animal)
                || entityBlacklist.isEntityBlackOrNotWhitelist(animal))
            return;

        CompoundTag persistentData = animal.getPersistentData();

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefault(persistentData, EAStrings.Tags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, speedMultiplier);
        boolean canAttackBack = NBTUtils.getBooleanOrPutDefault(persistentData, CAN_ATTACK_BACK, animal.getRandom().nextDouble() < fightBackChance);
        boolean playerScared = NBTUtils.getBooleanOrPutDefault(persistentData, PLAYER_SCARED, animal.getRandom().nextDouble() < playersScaredChance);
        boolean notTempted = NBTUtils.getBooleanOrPutDefault(persistentData, NOT_TEMPTED, animal.getRandom().nextDouble() < notTemptedChance);

        if (canAttackBack && !animal.isBaby()) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, movementSpeedMultiplier, true));
            animal.goalSelector.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof PanicGoal);
            if (knockback > 0d) {
                AttributeInstance kbAttribute = animal.getAttribute(Attributes.ATTACK_KNOCKBACK);
                if (kbAttribute != null)
                    kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", knockback, AttributeModifier.Operation.ADDITION));
            }
        }
        else if (playerScared) {
            EAAvoidEntityGoal<Player> avoidEntityGoal = new EAAvoidEntityGoal<>(animal, Player.class, (float) 16, (float) 8, 1.25, 1.1);
            animal.goalSelector.addGoal(1, avoidEntityGoal);
        }

        if (notTempted) {
            animal.goalSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof TemptGoal);
        }
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !groupFlee
                || !(event.getEntity() instanceof Animal animal)
                || entityBlacklist.isEntityBlackOrNotWhitelist(animal)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker))
            return;

        animal.level.getNearbyEntities(Animal.class, TargetingConditions.forNonCombat().ignoreLineOfSight(), animal, animal.getBoundingBox().inflate(groupFleeRange))
                .stream().filter(otherAnimal -> otherAnimal.getType().equals(animal.getType())).forEach(nearbyAnimal -> nearbyAnimal.setLastHurtByMob(attacker));
    }
}