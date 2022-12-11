package insane96mcp.enhancedai.modules.animal.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Animal Attacking", description = "Make animals fight back and no longer flee when attacked")
@LoadFeature(module = Modules.Ids.ANIMAL)
public class AnimalAttacking extends Feature {

    @Config
    @Label(name = "Animals Fight back", description = "If true, when attacked, animals will call other animals for help and attack back. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4")
    public static Boolean animalsFightBack = true;
    @Config
    @Label(name = "Animals No Longer Flee", description = "If true, when attacked, animals will no longer flee.")
    public static Boolean noMoreFlee = true;
    @Config(min = 0d, max = 4d)
    @Label(name = "Movement Speed Multiplier", description = "Movement speed multiplier when aggroed.")
    public static Double speedMultiplier = 1.35d;
    @Config(min = 0d, max = 128d)
    @Label(name = "Knockback", description = "Animals' knockback attribute will be set to this value.")
    public static Double knockback = 3.5d;
    @Config
    @Label(name = "Entity Blacklist", description = "Entities that shouldn't be affected by this feature")
    public static Blacklist entityBlacklist = new Blacklist(List.of(), false);

    private static final double BASE_ATTACK_DAMAGE = 4d;

    public AnimalAttacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
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

        if (animalsFightBack) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, movementSpeedMultiplier, false));
            if (knockback > 0d) {
                AttributeInstance kbAttribute = animal.getAttribute(Attributes.ATTACK_KNOCKBACK);
                if (kbAttribute != null)
                    kbAttribute.addPermanentModifier(new AttributeModifier("Animal knockback", knockback, AttributeModifier.Operation.ADDITION));
            }
        }

        if (noMoreFlee) {
            ArrayList<Goal> goalsToRemove = new ArrayList<>();
            for (WrappedGoal prioritizedGoal : animal.goalSelector.availableGoals) {
                if (!(prioritizedGoal.getGoal() instanceof PanicGoal goal))
                    continue;

                goalsToRemove.add(goal);
            }

            goalsToRemove.forEach(animal.goalSelector::removeGoal);
        }
    }
}