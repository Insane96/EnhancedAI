package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.PanicGoalAccessor;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ANIMAL, description = "Make animals panic when one is attacked. Only entity types in `enhancedai:animal/can_panic` tag will be affected by this feature.")
public class AnimalsPanic extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("animal/can_panic"));
    @Config(min = 1, description = "Range at which an animal alerts other animals to panic/attack.")
    public static Integer fleeRange = 24;

    public static EAIData<Double> PANIC_SPEED_MODIFIER;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        PANIC_SPEED_MODIFIER = EAIData.ofDouble(this.createDataKey("panic_speed_mod"), (mob, panicSpeedModifier) ->
				GoalHelper.getGoal(mob.goalSelector, PanicGoal.class)
					.ifPresent(goal -> ((PanicGoalAccessor)goal).setSpeedModifier(panicSpeedModifier)));
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Animal animal)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
				|| !animal.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        animal.level().getNearbyEntities(Animal.class, TargetingConditions.forNonCombat().ignoreLineOfSight(), animal, animal.getBoundingBox().inflate(fleeRange))
                .stream().filter(otherAnimal -> otherAnimal.getType().equals(animal.getType()))
                .forEach(nearbyAnimal -> nearbyAnimal.setLastHurtByMob(attacker));
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getEntity() instanceof Enemy
                || !(event.getEntity() instanceof Animal animal)
				|| !animal.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        GoalHelper.getGoal(animal.goalSelector, PanicGoal.class)
                .ifPresent(goal -> PANIC_SPEED_MODIFIER.applyIfAbsent(animal, ((PanicGoalAccessor)goal).getSpeedModifier()));
    }
}