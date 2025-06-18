package insane96mcp.enhancedai.data.mpr;

import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.mobspropertiesrandomness.data.json.property.MPRProperty;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class EAIChangeDataProperty extends MPRProperty {
    public EAIChangeDataProperty(List<MPRCondition> conditions) {
        super(conditions);
    }

    @Override
    public boolean apply(LivingEntity living) {
        return super.apply(living);
    }
}
