package insane96mcp.enhancedai.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Warden.class)
public abstract class WardenMixin extends Monster {

	protected WardenMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@ModifyConstant(method = "customServerAiStep", constant = {@Constant(intValue = 20)})
	private int onCustomServerAiStep(int range) {
		return (int) insane96mcp.enhancedai.modules.warden.Warden.increaseDarknessRange(range);
	}
}
