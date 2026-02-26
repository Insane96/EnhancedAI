package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin extends Goal {
	@Final
	@Shadow
	private double speedModifier;

	@Shadow
	private Path path;

	@Final
	@Shadow
	protected PathfinderMob mob;

	@Shadow private int ticksUntilNextAttack;

	@Shadow protected abstract boolean isTimeToAttack();

	@Shadow protected abstract void resetAttackCooldown();

	@ModifyExpressionValue(method = "canUse", at = @At(value = "CONSTANT", args = "longValue=20"))
	public long onLastCanUseCheck(long constant) {
        if (!Feature.isEnabled(MeleeAttacking.class))
            return constant;
		return 0L;
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"))
	public boolean onMoveTo(PathNavigation pathNavigation, Entity entity, double speedModifier) {
		Path path = this.mob.getNavigation().createPath(entity, 0);
		return path != null && this.mob.getNavigation().moveTo(path, speedModifier);
	}

	/*@Inject(method = "canContinueToUse", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
	public void onCanContinueToUse(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue() && this.mob.getTarget() != null) {
			this.path = this.mob.getNavigation().createPath(this.mob.getTarget(), 0);
			if (this.path != null) {
				this.mob.getNavigation().moveTo(this.path, this.speedModifier);
				cir.setReturnValue(true);
			}
		}
		cir.setReturnValue(true);
	}*/

	@ModifyExpressionValue(method = "canContinueToUse", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;followingTargetEvenIfNotSeen:Z"))
	public boolean onFollowingTargetEvenIfNotSeen_canContinueToUse(boolean value) {
		if (Feature.isEnabled(MeleeAttacking.class))
			return true;
		return value;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;followingTargetEvenIfNotSeen:Z"))
	public boolean onFollowingTargetEvenIfNotSeen_tick(boolean value) {
		if (Feature.isEnabled(MeleeAttacking.class))
			return true;
		return value;
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
