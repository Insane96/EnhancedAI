package insane96mcp.enhancedai.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class BetaStrafe {
    protected final PathfinderMob mob;
    protected boolean leftStrafe;
    protected double scale;

    protected double angle;

    public BetaStrafe(PathfinderMob mob, boolean leftStrafe, double scale) {
        this.mob = mob;
        this.leftStrafe = leftStrafe;
        this.scale = scale;
    }

    public void start() {
        if (this.mob.getTarget() == null)
            return;
        double dx = this.mob.getX() - this.mob.getTarget().getX();
        double dz = this.mob.getZ() - this.mob.getTarget().getZ();
        this.angle = Math.atan2(dz, dx);
    }

    public void tick() {
        if (this.mob.getTarget() == null
                || this.mob.hurtTime > 0)
            return;

        double movementSpeed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double dx = mob.getX() - this.mob.getTarget().getX();
        double dz = mob.getZ() - this.mob.getTarget().getZ();
        double distance = Math.hypot(dx, dz);
        distance = Math.max(distance, 1.5);

        double angleDelta = (movementSpeed / distance) * scale;
        this.angle += (leftStrafe ? angleDelta : -angleDelta);

        double strafeAngle = this.angle + (this.leftStrafe ? Math.PI / 2 : -Math.PI / 2);

        double vx = Math.cos(strafeAngle) * movementSpeed;
        double vz = Math.sin(strafeAngle) * movementSpeed;

        Vec3 mov = new Vec3(vx, this.mob.getDeltaMovement().y, vz).multiply(scale, 1, scale);
        this.mob.setDeltaMovement(mov);

        Direction direction = Direction.fromYRot(Math.toDegrees(this.angle) - 90);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(
                this.mob.getX() + mov.x,
                this.mob.getY(),
                this.mob.getZ() + mov.z
        ).move(direction);

        if (this.mob.horizontalCollision && this.mob.onGround())
            this.mob.getJumpControl().jump();
    }
}
