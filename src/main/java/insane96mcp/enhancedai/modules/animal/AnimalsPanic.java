package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.mixin.accessors.PanicGoalAccessor;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ANIMAL, description = "Make animals panic when one is attacked")
public class AnimalsPanic extends Feature {
    @Config(min = 1, description = "Range at which an animal alerts other animals to panic/attack.")
    public static Integer fleeRange = 24;
    @Config(min = 0, max = 1, description = "Chance for an animal to get the Panic AI")
    public static Double panicChance = 1d;

    public static EAIData<Boolean> PANIC;

    public static ResourceLocation PANIC_SPEED_MODIFIER;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        PANIC = EAIData.ofBool(this.createDataKey("panic"), (mob, panic) -> {
            if (!(mob instanceof PathfinderMob pathfinderMob))
                return;
            if (!GoalHelper.hasGoal(mob.goalSelector, PanicGoal.class))
                mob.goalSelector.addGoal(0, new PanicGoal(pathfinderMob, ModNBTData.get(mob, PANIC_SPEED_MODIFIER, Double.class)));
            if (!panic)
                GoalHelper.removeGoal(mob.goalSelector, PanicGoal.class);
        });
        PANIC_SPEED_MODIFIER = this.createDataKey("panic_speed_mod");
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Animal animal)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker))
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
                || !(event.getEntity() instanceof Animal animal))
            return;

        GoalHelper.getGoal(animal.goalSelector, PanicGoal.class)
                .ifPresent(goal -> ModNBTData.put(animal, PANIC_SPEED_MODIFIER, ((PanicGoalAccessor)goal).getSpeedModifier()));

        PANIC.applyIfAbsent(animal, animal.getRandom().nextDouble() < panicChance);
    }
}