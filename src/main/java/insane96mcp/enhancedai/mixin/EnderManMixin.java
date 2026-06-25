package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.module.enderman.LookAngerCone;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EnderMan.class)
public class EnderManMixin extends Monster {
	protected EnderManMixin(EntityType<? extends Monster> p_33002_, Level p_33003_) {
		super(p_33002_, p_33003_);
	}

	@ModifyConstant(method = "isLookingAtMe", constant = @Constant(doubleValue = 0.025))
	private double modifyLookAngerCone(double constant) {
		if (!Feature.isEnabled(LookAngerCone.class))
			return constant;
		return LookAngerCone.lookAngerCone;
	}
}
