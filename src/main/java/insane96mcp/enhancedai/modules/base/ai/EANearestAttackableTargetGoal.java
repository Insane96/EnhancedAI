package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class EANearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
	protected final Class<T> targetClass;
	protected int targetChance;
	protected LivingEntity nearestTarget;
	/** This filter is applied to the Entity search. Only matching entities will be targeted. */
	public TargetingConditions targetEntitySelector;

	private boolean xray;

	public EANearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean checkSight) {
		this(goalOwnerIn, targetClassIn, checkSight, false);
	}

	public EANearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn) {
		this(goalOwnerIn, targetClassIn, checkSight, nearbyOnlyIn, null);
	}

	public EANearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(goalOwnerIn, checkSight, nearbyOnlyIn);
		this.targetClass = targetClassIn;
		this.targetChance = 10;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
		this.targetEntitySelector = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(targetPredicate);
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		if (this.targetChance > 0 && this.mob.getRandom().nextInt(this.targetChance) != 0) {
			return false;
		}
		else {
			this.findNearestTarget();
			return this.nearestTarget != null;
		}
	}

	protected AABB getTargetableArea(double targetDistance) {
		return this.mob.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
	}

	protected void findNearestTarget() {
		if (this.targetClass != Player.class && this.targetClass != ServerPlayer.class) {
			this.nearestTarget = this.mob.level.getNearestEntity(this.targetClass, this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetableArea(this.getFollowDistance()));
		}
		else {
			this.nearestTarget = this.mob.level.getNearestPlayer(this.targetEntitySelector, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
		}

	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.mob.setTarget(this.nearestTarget);
		super.start();
	}

	public void setInstaTarget(boolean instaTarget) {
		this.targetChance = instaTarget ? 0 : 10;
	}

	public void setXray(boolean xray) {
		this.xray = xray;
		this.targetEntitySelector.ignoreLineOfSight();
	}

	public static class TargetGoal<T extends LivingEntity> extends EANearestAttackableTargetGoal<T> {
		public TargetGoal(Spider goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
			super(goalOwnerIn, targetClassIn, checkSight, nearbyOnlyIn, targetPredicate);
		}

		/**
		 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
		 * method as well.
		 */
		public boolean canUse() {
			float f = this.mob.getBrightness();
			return !(f >= 0.5F) && super.canUse();
		}
	}
}
