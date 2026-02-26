package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;

public class EAIShulkerNearestDefenseTargetGoal<T extends LivingEntity> extends EAINearestAttackableTarget<T> {
    public EAIShulkerNearestDefenseTargetGoal(Shulker goalOwnerIn, Class<T> targetClassIn, boolean checkSight, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(goalOwnerIn, targetClassIn, checkSight, nearbyOnlyIn, targetingConditions);
    }

    public boolean canUse() {
        return this.mob.getTeam() != null && super.canUse();
    }

    protected AABB getTargetSearchArea(double targetDistance) {
        final double fixedAxisExtra = 4.0D;
        final AABB box = this.mob.getBoundingBox();
        final Direction.Axis axis = ((Shulker) this.mob).getAttachFace().getAxis();

        return switch (axis) {
            case X -> box.inflate(fixedAxisExtra, targetDistance, targetDistance);
            case Z -> box.inflate(targetDistance, targetDistance, fixedAxisExtra);
            default -> box.inflate(targetDistance, fixedAxisExtra, targetDistance);
        };
    }
}