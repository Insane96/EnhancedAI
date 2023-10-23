package insane96mcp.enhancedai.modules.animal;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Animals Group Flee", description = "Make animals flee/fight back when one is attacked")
@LoadFeature(module = Modules.Ids.ANIMAL)
public class AnimalsGroupFlee extends Feature {
    @Config(min = 1)
    @Label(name = "Flee Range", description = "Range at which an animal alerts other animals.")
    public static Integer groupFleeRange = 24;

    public AnimalsGroupFlee(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onAttacked(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Animal animal)
                || !(event.getSource().getEntity() instanceof LivingEntity attacker))
            return;

        animal.level().getNearbyEntities(Animal.class, TargetingConditions.forNonCombat().ignoreLineOfSight(), animal, animal.getBoundingBox().inflate(groupFleeRange))
                .stream().filter(otherAnimal -> otherAnimal.getType().equals(animal.getType()))
                .forEach(nearbyAnimal -> nearbyAnimal.setLastHurtByMob(attacker));
    }
}