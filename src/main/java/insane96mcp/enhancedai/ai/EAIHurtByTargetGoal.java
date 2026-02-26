package insane96mcp.enhancedai.ai;

import insane96mcp.enhancedai.module.mobs.targeting.Targeting;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.List;

public class EAIHurtByTargetGoal extends TargetGoal {
	private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
	private boolean alertSameType;

	public Class<?>[] toIgnoreDamage;
	@Nullable
	public Class<?>[] toIgnoreAlert;
    private int unseenTicks;

    public EAIHurtByTargetGoal(Mob pMob, Class<?>... pToIgnoreDamage) {
		super(pMob, true);
		this.toIgnoreDamage = pToIgnoreDamage;
	}

	public boolean canUse() {
		//New check to prevent the mob from switching target if the current target is the same
		LivingEntity hypotheticalNewMob = this.mob.getLastHurtByMob();
		if (hypotheticalNewMob == null)
			return false;
		LivingEntity currentTarget = this.mob.getTarget();
		if (currentTarget != null && currentTarget == hypotheticalNewMob)
			return false;
		//New check to prefer players oven non-player entities if enabled
		if (currentTarget instanceof Player && !(hypotheticalNewMob instanceof Player) && Targeting.HURT_BY_PREFER_PLAYERS.get(this.mob))
			return false;

		//New check to not switch target if the current one is closer
		if (currentTarget != null && this.mob.distanceToSqr(currentTarget) < this.mob.distanceToSqr(hypotheticalNewMob))
			return false;

		//if (hypotheticalNewMob.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER))
		//	return false;

		for (Class<?> oclass : this.toIgnoreDamage) {
			if (oclass.isAssignableFrom(hypotheticalNewMob.getClass())) {
				return false;
			}
		}

		return this.canAttack(hypotheticalNewMob, HURT_BY_TARGETING);
	}

	public EAIHurtByTargetGoal setAlertOthers(Class<?>... pReinforcementTypes) {
		this.alertSameType = true;
		this.toIgnoreAlert = pReinforcementTypes;
		return this;
	}

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null)
            target = this.targetMob;

        if (target == null)
            return false;
        if (!this.mob.canAttack(target))
            return false;

        Team team = this.mob.getTeam();
        Team team1 = target.getTeam();
        if (team != null && team1 == team)
            return false;

        double visibilityPercent = target.getVisibilityPercent(this.mob);
        double range = Math.max(this.getFollowDistance() * visibilityPercent, 2.0D);
        if (this.mob.distanceToSqr(target) > range * range)
            return false;
        if (this.mustSee) {
            if (this.mob.getSensing().hasLineOfSight(target))
                this.unseenTicks = 0;
            else if (++this.unseenTicks > reducedTickDelay(this.unseenMemoryTicks))
                return false;
        }

        this.mob.setTarget(target);
        return true;

    }

	public void start() {
		this.mob.setTarget(this.mob.getLastHurtByMob());
		this.targetMob = this.mob.getTarget();
		this.unseenMemoryTicks = 300;
        this.unseenTicks = 0;
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

	public void preventInfighting() {
		if (ArrayUtils.contains(this.toIgnoreDamage, Enemy.class))
			this.toIgnoreDamage = ArrayUtils.removeElement(this.toIgnoreDamage, Enemy.class);
	}

	public void allowInfighting() {
		if (!ArrayUtils.contains(this.toIgnoreDamage, Enemy.class))
			this.toIgnoreDamage = ArrayUtils.add(this.toIgnoreDamage, Enemy.class);
	}
}
