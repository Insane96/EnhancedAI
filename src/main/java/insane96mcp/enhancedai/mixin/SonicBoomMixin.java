package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.enhancedai.modules.warden.WardenFeature;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SonicBoom.class)
public abstract class SonicBoomMixin extends Behavior<Warden> {

	public SonicBoomMixin(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
		super(pEntryCondition);
	}

	/*@ModifyExpressionValue(method = "lambda$tick$1", at = @At(value = "CONSTANT", args = {"doubleValue=15.0"}))
	private static double enhancedai$onRange1(double range) {
		return WardenFeature.increaseSonicBoomRange(range);
	}

	@ModifyExpressionValue(method = "lambda$tick$1", at = @At(value = "CONSTANT", args = {"doubleValue=20.0"}))
	private static double enhancedai$onRange2(double range) {
		return WardenFeature.increaseSonicBoomRange(range);
	}*/

	@ModifyExpressionValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z", at = @At(value = "CONSTANT", args = {"doubleValue=15.0"}))
	private double enhancedai$onExtraConditionsRange1(double range) {
		return WardenFeature.increaseSonicBoomRange(range);
	}

	@ModifyExpressionValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z", at = @At(value = "CONSTANT", args = {"doubleValue=20.0"}))
	private double enhancedai$onExtraConditionsRange2(double range) {
		return WardenFeature.increaseSonicBoomRange(range);
	}
}
