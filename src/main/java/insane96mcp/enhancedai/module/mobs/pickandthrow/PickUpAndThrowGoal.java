package insane96mcp.enhancedai.module.mobs.pickandthrow;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PickUpAndThrowGoal extends Goal {
    final Mob mob;
    final TargetingConditions targetingConditions;
    Mob pickUp;
    int unreachableTime;
    int cooldown;

    LivingEntity actualTarget;

    public PickUpAndThrowGoal(Mob mob) {
        this.mob = mob;
        this.targetingConditions = TargetingConditions.forNonCombat()
                .range(this.getFollowDistance())
                .selector(livingEntity -> !livingEntity.isPassenger());
        this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!PickUpAndThrow.CAN_PICK_UP_DATA.has(this.mob)
                || this.mob.getTarget() == null
                || this.mob.getTarget().distanceTo(this.mob) < PickUpAndThrow.MIN_DISTANCE_TO_PICK_UP.get(this.mob)
                || --this.cooldown > 0)
            return false;

        if (this.mob.getFirstPassenger() instanceof Mob passenger) {
			this.pickUp = passenger;
			return true;
		}

        this.targetingConditions.range(this.getFollowDistance());
        TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(PickUpAndThrow.CAN_PICK_UP_DATA.get(this.mob)));
        this.pickUp = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(Mob.class,
                        this.mob.getBoundingBox().inflate(this.getFollowDistance()),
                        living -> living.getType().is(tag)),
                this.targetingConditions,
                this.mob,
                this.mob.getX(),
                this.mob.getEyeY(),
                this.mob.getZ());
        this.actualTarget = this.mob.getTarget();
        return this.pickUp != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.pickUp != null && this.pickUp.isAlive() && this.actualTarget != null && this.actualTarget.isAlive();
    }

    @Override
    public void start() {
        this.mob.getLookControl().setLookAt(this.pickUp);
        this.mob.getNavigation().stop();
        this.mob.getNavigation().moveTo(this.pickUp, PickUpAndThrow.SPEED_MODIFIER_TO_PICK_UP.get(this.mob));
        this.pickUp.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.pickUp = null;
        this.unreachableTime = 0;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void tick() {
        if (!this.mob.isVehicle()) {
            this.pickUp.getNavigation().stop();
            if (this.mob.getNavigation().isDone())
                this.mob.getNavigation().moveTo(this.pickUp, PickUpAndThrow.SPEED_MODIFIER_TO_PICK_UP.get(this.mob));
            if (this.mob.distanceToSqr(this.pickUp) <= 4f) {
                this.pickUp.startRiding(this.mob, false);
                this.pickUp.getNavigation().moveTo(this.actualTarget, 1f);
                this.cooldown = this.adjustedTickDelay(20);
                //this.stop();
            }
        }
        else {
            if (this.actualTarget != null && this.mob.getSensing().hasLineOfSight(this.actualTarget) && --this.cooldown <= 0 && this.actualTarget.distanceTo(this.mob) <= PickUpAndThrow.MAX_DISTANCE_TO_THROW.get(this.mob)) {
                double distanceY = this.actualTarget.getY() - this.pickUp.getY();
                double distanceX = this.actualTarget.getX() - this.pickUp.getX();
                double distanceZ = this.actualTarget.getZ() - this.pickUp.getZ();
                double distanceXZ = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);

                Vec3 motion = new Vec3(distanceX * 0.1d, Mth.clamp(distanceY, 4d, 40d) / 10d + distanceXZ / 100d, distanceZ * 0.1d);
                this.pickUp.stopRiding();
                this.pickUp.setDeltaMovement(motion);
                this.mob.playSound(SoundEvents.AXE_STRIP, 3f, 1.5F);

                this.cooldown = this.adjustedTickDelay(PickUpAndThrow.COOLDOWN.get(this.mob));
                this.stop();
            }
        }
        if (++this.unreachableTime > this.adjustedTickDelay(120)) {
            this.cooldown = this.adjustedTickDelay(PickUpAndThrow.COOLDOWN.get(this.mob));
            this.stop();
        }
    }
}
