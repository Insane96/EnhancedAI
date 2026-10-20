package insane96mcp.enhancedai.module.mobs.teleportanticheese;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class TeleportAntiCheeseGoal extends Goal {

    private final Mob mob;
    private LivingEntity target;

    private int awayFromTargetTick = 0;

    public TeleportAntiCheeseGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        this.target = this.mob.getTarget();
        //If the mob is moving it's not stuck, so reset the ticks
        if (!this.mob.getNavigation().isDone()) {
            this.awayFromTargetTick = 0;
            return false;
        }
        if (this.target == null
                || this.target.getType().is(TeleportAntiCheese.CANT_BE_TELEPORTED)
                || this.target.distanceTo(this.mob) > this.mob.getAttributeValue(Attributes.FOLLOW_RANGE))
            return false;

        if (!this.mob.isWithinMeleeAttackRange(this.target) || !this.mob.hasLineOfSight(this.target)) {
            this.awayFromTargetTick++;
        }
        else {
            this.awayFromTargetTick = 0;
        }

        return this.awayFromTargetTick > this.adjustedTickDelay(50);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void start() {
        if (this.target.isSleeping())
            this.target.stopSleeping();
        if (this.target.isPassenger())
            this.target.stopRiding();
        for (int i = 0; i < 16; ++i) {
            double x = this.mob.getX() + (this.mob.getRandom().nextDouble() - 0.5D) * 8.0D;
            double y = Mth.clamp(this.mob.getY() + (double)(this.mob.getRandom().nextInt(8) - 4), this.mob.level().getMinBuildHeight(), this.mob.level().getMinBuildHeight() + ((ServerLevel)this.mob.level()).getLogicalHeight() - 1);
            double z = this.mob.getZ() + (this.mob.getRandom().nextDouble() - 0.5D) * 8.0D;

            Vec3 vec3 = this.target.position();
            this.target.level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this.target));
            if (this.target.randomTeleport(x, y, z, true)) {
                this.mob.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1f, 0.5f);
                break;
            }
        }
        this.awayFromTargetTick = 0;
    }
}
