package insane96mcp.enhancedai.module.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.PanicGoalAccessor;
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
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = EAIModules.ANIMAL, description = "Make animals panic when one is attacked. Only entity types in `enhancedai:animal/can_panic` tag will be affected by this feature.")
public class AnimalsPanic extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal/can_panic"));
    @Config(min = 1, description = "Range at which an animal alerts other animals to panic/attack.")
    public static Integer fleeRange = 16;
	@Config(description = "The flee range will be this value if the animal can see the other animals")
	public static Integer fleeRangeIfSeen = 32;

    public static EAIData<Double> PANIC_SPEED_MODIFIER;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        PANIC_SPEED_MODIFIER = EAIData.ofDouble(this.createDataKey("panic_speed_mod"), (mob, panicSpeedModifier) ->
				GoalHelper.getGoal(mob.goalSelector, PanicGoal.class)
					.ifPresent(goal -> ((PanicGoalAccessor)goal).setSpeedModifier(panicSpeedModifier)));
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent.Pre event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Animal animal)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
				|| !animal.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        animal.level().getNearbyEntities(Animal.class, TargetingConditions.forNonCombat().ignoreLineOfSight(), animal, animal.getBoundingBox().inflate(fleeRangeIfSeen))
                .stream().filter(otherAnimal -> {
					if (!otherAnimal.getType().equals(animal.getType()))
						return false;
					if (!otherAnimal.getSensing().hasLineOfSight(animal) && otherAnimal.distanceToSqr(animal) > fleeRange * fleeRange)
						return false;
					return otherAnimal.distanceTo(animal) < fleeRangeIfSeen;
				})
                .forEach(nearbyAnimal -> nearbyAnimal.setLastHurtByMob(attacker));
    }

    //Low priority so other mods can set persistent data
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal)
				|| !animal.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        GoalHelper.getGoal(animal.goalSelector, PanicGoal.class)
                .ifPresent(goal -> PANIC_SPEED_MODIFIER.applyIfAbsent(animal, ((PanicGoalAccessor)goal).getSpeedModifier()));
    }
}