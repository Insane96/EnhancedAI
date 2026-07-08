package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.enhancedai.module.mobs.MeleeAttacking;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin extends Goal {
	@Final
	@Shadow
	private double speedModifier;

	@Final
	@Shadow
	protected PathfinderMob mob;

	@ModifyExpressionValue(method = "canUse", at = @At(value = "CONSTANT", args = "longValue=20"))
	public long onLastCanUseCheck(long constant) {
        if (!Feature.isEnabled(MeleeAttacking.class))
            return constant;
		return 0L;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"))
	public boolean onMoveTo(PathNavigation instance, Entity entity, double speed, Operation<Boolean> original) {
		if (!Feature.isEnabled(MeleeAttacking.class))
			return original.call(instance, entity, speed);
		Path path = this.mob.getNavigation().createPath(entity, 0);
		return path != null && this.mob.getNavigation().moveTo(path, speedModifier);
	}

	@ModifyExpressionValue(method = "canContinueToUse", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;followingTargetEvenIfNotSeen:Z"))
	public boolean onFollowingTargetEvenIfNotSeen_canContinueToUse(boolean value) {
        if (!Feature.isEnabled(MeleeAttacking.class))
            return value;
        return true;
    }

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;followingTargetEvenIfNotSeen:Z"))
	public boolean onFollowingTargetEvenIfNotSeen_tick(boolean value) {
        if (!Feature.isEnabled(MeleeAttacking.class))
            return value;
        return true;
    }

	@ModifyReturnValue(method = "getAttackInterval", at = @At(value = "RETURN"))
	public int onGetAttackCooldown(int original) {
		if (!MeleeAttacking.shouldUseAttackSpeedAttribute())
            return original;
        return this.enhancedAI$getTicksUntilNextAttack();
    }

	@ModifyExpressionValue(method = "resetAttackCooldown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;adjustedTickDelay(I)I"))
	public int onResetAttackCooldown(int original) {
		if (!MeleeAttacking.shouldUseAttackSpeedAttribute())
            return original;
        return this.enhancedAI$getTicksUntilNextAttack();
    }

	@Unique
	private int enhancedAI$getTicksUntilNextAttack() {
		double attackSpeed = this.mob.getAttributeValue(Attributes.ATTACK_SPEED);
		if (attackSpeed <= 0f)
			attackSpeed = this.mob.getAttributeValue(Attributes.ATTACK_SPEED);
		attackSpeed *= MeleeAttacking.attackSpeed$multiplier.getByDifficulty(this.mob.level());
		if (attackSpeed > MeleeAttacking.attackSpeed$maximum)
			attackSpeed = MeleeAttacking.attackSpeed$maximum;
		return (int) (1d / attackSpeed * 20d);
	}
}
