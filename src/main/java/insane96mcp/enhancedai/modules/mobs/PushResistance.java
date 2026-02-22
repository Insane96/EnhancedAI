package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.setup.EAIAttributes;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Adds a new push resistance attribute to mobs. This is added to some vanilla mobs through MPR data pack")
public class PushResistance extends Feature {
    public static void attribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!event.has(entityType, EAIAttributes.PUSH_RESISTANCE.get()))
                event.add(entityType, EAIAttributes.PUSH_RESISTANCE.get());
        }
    }

    public static double getPushResistance(Entity entity) {
        if (!Feature.isEnabled(PushResistance.class)
                || !(entity instanceof LivingEntity living)
                || living.getAttribute(EAIAttributes.PUSH_RESISTANCE.get()) == null)
            return 1d;

        double resistance = living.getAttributeValue(EAIAttributes.PUSH_RESISTANCE.get());
        return 1d - resistance;
    }
}
