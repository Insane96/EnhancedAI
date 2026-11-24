package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.module.mobs.targeting.Targeting;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomAttackPlayerTargetGoal")
public class PhantomAttackPlayerTargetGoalMixin {
	/// Vanilla searches players only 16 blocks horizontally from the phantom, so phantoms circling far from the player never find them.
	/// Use the phantom's follow range as horizontal search range instead.
	@WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getNearbyPlayers(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
	private List<Player> enhancedai$followRangeSearch(Level level, TargetingConditions conditions, LivingEntity phantom, AABB area, Operation<List<Player>> original) {
		if (Feature.isEnabled(Targeting.class)
				&& phantom.getAttribute(Attributes.FOLLOW_RANGE) != null) {
			double range = phantom.getAttributeValue(Attributes.FOLLOW_RANGE);
			area = phantom.getBoundingBox().inflate(range, 64d, range);
		}
		return original.call(level, conditions, phantom, area);
	}
}
