package insane96mcp.enhancedai.modules.animal.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
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

    @Config
    @Label(name = "Group Flee", description = "If true, when an animal is attacked, all the animals around will flee.")
    public static Boolean groupFlee = true;
    @Config
    @Label(name = "Fight back chance", description = "Animals have this percentage chance to be able to fight back instead of fleeing. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4")
    public static Double animalsFightBackChance = 0.2d;
    @Config(min = 0d, max = 4d)
    @Label(name = "Movement Speed Multiplier", description = "Movement speed multiplier when aggroed.")
    public static Double speedMultiplier = 1.35d;
    @Config(min = 0d, max = 128d)
    @Label(name = "Knockback", description = "Animals' knockback attribute will be set to this value.")
    public static Double knockback = 3d;
    @Config
    @Label(name = "Entity Blacklist", description = "Entities that shouldn't be affected by this feature")
    public static Blacklist entityBlacklist = new Blacklist(List.of(
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:llama"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:trader_llama"),
            new IdTagMatcher(IdTagMatcher.Type.ID, "minecraft:bee")
    ), false);

    private static final double BASE_ATTACK_DAMAGE = 4d;

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
        boolean canAttackBack = NBTUtils.getBooleanOrPutDefault(persistentData, CAN_ATTACK_BACK, animal.getRandom().nextDouble() < animalsFightBackChance);

        if (canAttackBack && !animal.isBaby()) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, movementSpeedMultiplier, true));
            if (knockback > 0d) {
                AttributeInstance kbAttribute = animal.getAttribute(Attributes.ATTACK_KNOCKBACK);
                if (kbAttribute != null)
                    kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", knockback, AttributeModifier.Operation.ADDITION));
            }
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

        animal.level.getNearbyEntities(Animal.class, TargetingConditions.forNonCombat().ignoreLineOfSight(), animal, animal.getBoundingBox().inflate(12d))
                .forEach(nearbyAnimal -> nearbyAnimal.setLastHurtByMob(attacker));
    }
}