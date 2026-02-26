package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.enhancedai.module.warden.WardenSonicBoomRange;
import net.minecraft.server.level.ServerLevel;
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

	@ModifyExpressionValue(method = "lambda$tick$1", at = @At(value = "CONSTANT", args = {"doubleValue=15.0"}))
	private static double enhancedai$onRange1(double original, @Local(argsOnly = true) Warden owner) {
		return WardenSonicBoomRange.changeRange(owner, original);
	}

	@ModifyExpressionValue(method = "lambda$tick$1", at = @At(value = "CONSTANT", args = {"doubleValue=20.0"}))
	private static double enhancedai$onRange2(double range, @Local(argsOnly = true) Warden owner) {
		return WardenSonicBoomRange.changeRange(owner, range);
	}

	@ModifyExpressionValue(method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)V", at = @At(value = "CONSTANT", args = {"floatValue=3.0"}))
	private float enhancedai$increasePlaySoundVolume1(float original, ServerLevel pLevel, Warden pEntity) {
		return 5f;
	}

	@ModifyExpressionValue(method = "lambda$tick$2", at = @At(value = "CONSTANT", args = {"floatValue=3.0"}))
	private static float enhancedai$increasePlaySoundVolume2(float original) {
		return 5f;
	}

	@ModifyExpressionValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z", at = @At(value = "CONSTANT", args = {"doubleValue=15.0"}))
	private double enhancedai$onExtraConditionsRange1(double range, ServerLevel level, Warden owner) {
		return WardenSonicBoomRange.changeRange(owner, range);
	}

	@ModifyExpressionValue(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z", at = @At(value = "CONSTANT", args = {"doubleValue=20.0"}))
	private double enhancedai$onExtraConditionsRange2(double range, ServerLevel level, Warden owner) {
		return WardenSonicBoomRange.changeRange(owner,range);
	}
}
