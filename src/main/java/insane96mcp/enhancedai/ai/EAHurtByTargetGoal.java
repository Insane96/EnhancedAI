package insane96mcp.enhancedai.ai;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class EAHurtByTargetGoal extends TargetGoal {
	private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
	private boolean alertSameType;
	/** Store the previous revengeTimer value */
	public final Class<?>[] toIgnoreDamage;
	@Nullable
	public Class<?>[] toIgnoreAlert;

	public EAHurtByTargetGoal(PathfinderMob pMob, Class<?>... pToIgnoreDamage) {
		super(pMob, true);
		this.toIgnoreDamage = pToIgnoreDamage;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	public boolean canUse() {
		//New check to prevent the mob from switching target if the current target is the same
		LivingEntity hypotheticalNewMob = this.mob.getLastHurtByMob();
		if (hypotheticalNewMob == null)
			return false;
		LivingEntity currentTarget = this.mob.getTarget();
		if (currentTarget != null && currentTarget == hypotheticalNewMob)
			return false;
		//New check to prefer players oven non-player entities
		if (currentTarget instanceof Player && !(hypotheticalNewMob instanceof Player))
			return false;

		//New check to not switch target if the current one is closer
		if (currentTarget != null && this.mob.distanceToSqr(currentTarget) < this.mob.distanceToSqr(hypotheticalNewMob))
			return false;

		if (hypotheticalNewMob.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER))
			return false;

		for (Class<?> oclass : this.toIgnoreDamage) {
			if (oclass.isAssignableFrom(hypotheticalNewMob.getClass())) {
				return false;
			}
		}

		return this.canAttack(hypotheticalNewMob, HURT_BY_TARGETING);
	}

	public EAHurtByTargetGoal setAlertOthers(Class<?>... pReinforcementTypes) {
		this.alertSameType = true;
		this.toIgnoreAlert = pReinforcementTypes;
		return this;
	}

	public void start() {
		this.mob.setTarget(this.mob.getLastHurtByMob());
		this.targetMob = this.mob.getTarget();
		this.unseenMemoryTicks = 300;
		if (this.alertSameType) {
			this.alertOthers();
		}

		super.start();
	}

	@Override
	public void tick() {
		if (this.canUse())
			this.start();
	}

	protected void alertOthers() {
		double d0 = this.getFollowDistance();
		AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, d0, d0);
		List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), aabb, EntitySelector.NO_SPECTATORS);
		for (Mob mobNearby : list) {
			if (this.mob == mobNearby
					|| mobNearby.getTarget() != null
					|| (this.mob instanceof TamableAnimal && ((TamableAnimal) this.mob).getOwner() != ((TamableAnimal) mobNearby).getOwner())
					|| mobNearby.isAlliedTo(this.mob.getLastHurtByMob()))
				continue;

			if (this.toIgnoreAlert != null) {
				boolean isClassToIgnore = false;
				for (Class<?> oclass : this.toIgnoreAlert) {
					if (mobNearby.getClass() == oclass) {
						isClassToIgnore = true;
						break;
					}
				}
				if (isClassToIgnore)
					continue;
			}
			this.alertOther(mobNearby, this.mob.getLastHurtByMob());
		}
	}

	protected void alertOther(Mob pMob, LivingEntity pTarget) {
		pMob.setTarget(pTarget);
	}
}
