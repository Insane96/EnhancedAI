package insane96mcp.enhancedai.module.mobs.patrol;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PatrolGoal extends Goal {
    Mob owner;
    private Vec3 pos;
    private int cooldown;

    public PatrolGoal(Mob owner) {
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        this.owner = owner;
    }

    @Override
    public boolean canUse() {
        if (--this.cooldown > 0
                || !Patrol.POS.has(this.owner)
                || !Patrol.RANGE.has(this.owner))
            return false;
        this.pos = Vec3.atCenterOf(Patrol.POS.get(this.owner));
        return this.isOutOfRange(1f);
    }

    @Override
    public boolean canContinueToUse() {
        //Stop when the path ends (e.g. partial path), so the goal can restart with a new path from the new position
        return !this.owner.getNavigation().isDone() && this.isOutOfRange(0.15f);
    }

    @Override
    public void start() {
        //If the position can't be reached try again later, so the mob is not stuck in the goal
        if (!this.owner.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, (int) (Patrol.RANGE.get(this.owner) * 0.1d), 1f))
            this.cooldown = this.adjustedTickDelay(100);
    }

    @Override
    public void stop() {
        this.owner.getNavigation().stop();
        if (this.cooldown <= 0 && this.isOutOfRange(1f))
            this.cooldown = this.adjustedTickDelay(20);
    }

    private boolean isOutOfRange(float rangeSqrMultiplier) {
        double range = Patrol.RANGE.get(this.owner);
        range *= range;
        return this.owner.distanceToSqr(this.pos) > range * rangeSqrMultiplier;
    }
}
