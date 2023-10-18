package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EATags;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Animals Scared Attack", description = "Make animals fight back or be scared by players. Use the entity type tag enhancedai:can_fight_back and enhancedai:can_be_scared_by_players to change animals")
@LoadFeature(module = Modules.Ids.ANIMAL)
public class AnimalScaredAttack extends Feature {
    public static final TagKey<EntityType<?>> CAN_FIGHT_BACK = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_fight_back"));
    public static final TagKey<EntityType<?>> CAN_BE_SCARED_BY_PLAYERS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_be_scared_by_players"));
    public static final String CAN_ATTACK_BACK = EnhancedAI.RESOURCE_PREFIX + "can_attack_back";
    public static final String PLAYER_SCARED = EnhancedAI.RESOURCE_PREFIX + "player_scared";

    @Config
    @Label(name = "Fight back chance", description = "Animals have this percentage chance to be able to fight back instead of fleeing. Animals have a slightly bigger range to attack. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 3.")
    public static Double fightBackChance = 0.3d;
    @Config
    @Label(name = "Players Scared chance", description = "Animals have this percentage chance to be scared by players and run away. Fight back chance has priority over this.")
    public static Double playersScaredChance = 0.4d;
    @Config(min = 0d, max = 4d)
    @Label(name = "Movement Speed Multiplier", description = "Movement speed multiplier when aggroed.")
    public static Double speedMultiplier = 1.3d;
    @Config(min = 0d, max = 128d)
    @Label(name = "Knockback", description = "Animals' knockback attribute will be set to this value.")
    public static Double knockback = 1.5d;
    @Config
    @Label(name = "Knockback size based", description = "Animals' knockback attribute will be increased/decreased based on the side of the mob.")
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

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefault(persistentData, EATags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, speedMultiplier);
        boolean canAttackBack = NBTUtils.getBooleanOrPutDefault(persistentData, CAN_ATTACK_BACK, animal.getType().is(CAN_FIGHT_BACK) && animal.getRandom().nextDouble() < fightBackChance);
        boolean playerScared = NBTUtils.getBooleanOrPutDefault(persistentData, PLAYER_SCARED, !canAttackBack && animal.getType().is(CAN_BE_SCARED_BY_PLAYERS) && animal.getRandom().nextDouble() < playersScaredChance);

        if (canAttackBack && !animal.isBaby()) {
            animal.targetSelector.addGoal(1, (new HurtByTargetGoal(animal)).setAlertOthers());
            animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, movementSpeedMultiplier, true));
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
        }
        else if (playerScared) {
            EAAvoidEntityGoal<Player> avoidEntityGoal = new EAAvoidEntityGoal<>(animal, Player.class, (float) 16, (float) 8, 1.25, 1.1);
            animal.goalSelector.addGoal(1, avoidEntityGoal);
        }
    }
}