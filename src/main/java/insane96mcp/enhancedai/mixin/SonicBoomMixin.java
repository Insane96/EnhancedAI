package insane96mcp.enhancedai.mixin;

import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Map;

@Mixin(SonicBoom.class)
public abstract class SonicBoomMixin extends Behavior<Warden> {

	public SonicBoomMixin(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
		super(pEntryCondition);
	}

	//TODO MixinExtras
	@ModifyConstant(method = "lambda$tick$1", constant = {@Constant(doubleValue = 15d), @Constant(doubleValue = 20d)})
	private static double onRange(double range) {
		return insane96mcp.enhancedai.modules.warden.Warden.increaseSonicBoomRange(range);
	}

	@ModifyConstant(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z", constant = {@Constant(doubleValue = 15d), @Constant(doubleValue = 20d)})
	private double onExtraConditionsRange(double range) {
		return insane96mcp.enhancedai.modules.warden.Warden.increaseSonicBoomRange(range);
	}
}
