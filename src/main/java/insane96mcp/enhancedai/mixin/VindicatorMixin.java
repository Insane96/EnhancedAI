package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.enhancedai.module.mobs.OpenDoors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Vindicator.class)
public abstract class VindicatorMixin extends AbstractIllager {
	protected VindicatorMixin(EntityType<? extends AbstractIllager> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Definition(id = "flag", local = @Local(type = boolean.class))
	@Expression("flag")
	@ModifyExpressionValue(method = "customServerAiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
	public boolean onSetCanOpenDoors(boolean original) {
		if (OpenDoors.shouldBeAbleToOpenDoors((Vindicator) (Object) this))
			return OpenDoors.CAN_OPEN_DOORS.get(this);
		return original;
	}
}
