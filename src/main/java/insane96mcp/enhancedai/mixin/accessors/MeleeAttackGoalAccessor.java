package insane96mcp.enhancedai.mixin.accessors;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MeleeAttackGoal.class)
public interface MeleeAttackGoalAccessor {
    @Accessor
    double getSpeedModifier();
    @Accessor
    @Mutable
    void setSpeedModifier(double speedModifier);
}
