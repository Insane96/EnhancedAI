package insane96mcp.enhancedai.modules.mobs.riding;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class SearchMountGoal extends Goal {
    final Mob mob;
    final TargetingConditions targetingConditions;
    LivingEntity mount;
    int unreachableTime;
    int cooldown;
    public SearchMountGoal(Mob mob) {
        this.mob = mob;
        this.targetingConditions = TargetingConditions.forNonCombat().range(this.getFollowDistance()).selector(livingEntity -> livingEntity.getType().is(Riding.CAN_BE_MOUNTED) && !livingEntity.isVehicle());
        this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getVehicle() != null)
            return false;

        if (--this.cooldown > 0)
            return false;
        this.mount = this.mob.level().getNearestEntity(LivingEntity.class, this.targetingConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.mob.getBoundingBox().inflate(this.getFollowDistance()));
        return this.mount != null;
    }

    @Override
    public void stop() {
        this.mount = null;
        this.unreachableTime = 0;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void tick() {
        if (this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().moveTo(this.mount, 1d);
        }
        if (this.mob.distanceToSqr(this.mount) <= 3f) {
            this.mob.startRiding(this.mount, true);
        }
        if (++this.unreachableTime > reducedTickDelay(200)) {
            this.cooldown = reducedTickDelay(200);
            this.stop();
        }
    }
}
