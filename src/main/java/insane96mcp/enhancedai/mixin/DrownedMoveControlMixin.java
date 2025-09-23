package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.enhancedai.modules.mobs.Swimmers;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Drowned.DrownedMoveControl.class)
public abstract class DrownedMoveControlMixin extends MoveControl {
	public DrownedMoveControlMixin(Mob pMob) {
		super(pMob);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;MOVEMENT_SPEED:Lnet/minecraft/world/entity/ai/attributes/Attribute;"))
    public Attribute enhancedai$changeSwimSpeedAttribute(Attribute original) {
		if (!Feature.isEnabled(Swimmers.class))
			return original;
		return ForgeMod.SWIM_SPEED.get();
    }
}
