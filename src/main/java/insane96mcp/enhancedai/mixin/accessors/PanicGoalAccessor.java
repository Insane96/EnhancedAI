package insane96mcp.enhancedai.mixin.accessors;

import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PanicGoal.class)
public interface PanicGoalAccessor {
	@Accessor
	double getSpeedModifier();
	@Accessor
	@Mutable
	void setSpeedModifier(double speedModifier);
}
