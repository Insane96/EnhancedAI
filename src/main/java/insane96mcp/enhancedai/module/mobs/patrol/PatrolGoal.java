package insane96mcp.enhancedai.module.mobs.patrol;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PatrolGoal extends Goal {
    Mob owner;
    private Vec3 pos;

    public PatrolGoal(Mob owner) {
        this.setFlags(EnumSet.allOf(Flag.class));
        this.owner = owner;
    }

    @Override
    public boolean canUse() {
        this.pos = Vec3.atCenterOf(Patrol.POS.get(this.owner));
        double range = Patrol.RANGE.get(this.owner);
        range *= range;
        return this.owner.distanceToSqr(this.pos) > range;
    }

    @Override
    public boolean canContinueToUse() {
        double range = Patrol.RANGE.get(this.owner);
        range *= range;
        return this.owner.distanceToSqr(this.pos) > range * 0.15f;
    }

    @Override
    public void start() {
        this.owner.getNavigation().moveTo(this.pos.x, this.pos.y, this.pos.z, (int) (Patrol.RANGE.get(this.owner) * 0.1d), 1f);
    }
}
