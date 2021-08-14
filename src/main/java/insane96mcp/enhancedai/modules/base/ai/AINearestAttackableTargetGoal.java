package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class AINearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
	protected final Class<T> targetClass;
	protected int targetChance;
	protected LivingEntity nearestTarget;
	/** This filter is applied to the Entity search. Only matching entities will be targeted. */
	protected EntityPredicate targetEntitySelector;

	private boolean xray;

	public AINearestAttackableTargetGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, boolean checkSight) {
		this(goalOwnerIn, targetClassIn, checkSight, false);
	}

	public AINearestAttackableTargetGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn) {
		this(goalOwnerIn, targetClassIn, checkSight, nearbyOnlyIn, null);
	}

	public AINearestAttackableTargetGoal(MobEntity goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(goalOwnerIn, checkSight, nearbyOnlyIn);
		this.targetClass = targetClassIn;
		this.targetChance = 10;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
		EntityPredicate predicate = (new EntityPredicate()).setDistance(this.getTargetDistance()).setCustomPredicate(targetPredicate);
		this.targetEntitySelector = predicate;
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean shouldExecute() {
		if (this.targetChance > 0 && this.goalOwner.getRNG().nextInt(this.targetChance) != 0) {
			return false;
		}
		else {
			this.findNearestTarget();
			return this.nearestTarget != null;
		}
	}

	protected AxisAlignedBB getTargetableArea(double targetDistance) {
		return this.goalOwner.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
	}

	protected void findNearestTarget() {
		if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
			this.nearestTarget = this.goalOwner.world.func_225318_b(this.targetClass, this.targetEntitySelector, this.goalOwner, this.goalOwner.getPosX(), this.goalOwner.getPosYEye(), this.goalOwner.getPosZ(), this.getTargetableArea(this.getTargetDistance()));
		}
		else {
			this.nearestTarget = this.goalOwner.world.getClosestPlayer(this.targetEntitySelector, this.goalOwner, this.goalOwner.getPosX(), this.goalOwner.getPosYEye(), this.goalOwner.getPosZ());
		}

	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		this.goalOwner.setAttackTarget(this.nearestTarget);
		super.startExecuting();
	}

	public void setNearestTarget(@Nullable LivingEntity target) {
		this.nearestTarget = target;
	}

	public void setInstaTarget(boolean instaTarget) {
		this.targetChance = instaTarget ? 0 : 10;
	}

	public void setXray(boolean xray) {
		this.xray = xray;
		this.targetEntitySelector.setLineOfSiteRequired();
	}
}
