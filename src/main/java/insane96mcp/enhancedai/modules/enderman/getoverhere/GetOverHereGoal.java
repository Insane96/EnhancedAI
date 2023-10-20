package insane96mcp.enhancedai.modules.enderman.getoverhere;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;

public class GetOverHereGoal extends Goal {

    private final EnderMan enderman;
    private LivingEntity target;

    private int awayFromTargetTick = 0;

    public GetOverHereGoal(EnderMan enderman) {
        this.enderman = enderman;
    }

    @Override
    public boolean canUse() {
        if (this.enderman.getTarget() == null)
            return false;

        if (this.enderman.getTarget().distanceToSqr(this.enderman) >= 8d) {
            this.awayFromTargetTick++;
        }
        else {
            this.awayFromTargetTick = 0;
        }

        return this.awayFromTargetTick > reducedTickDelay(50);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void start() {
        this.target = this.enderman.getTarget();
        if (this.target instanceof ServerPlayer player) {
            if (player.isSleeping()) {
                player.stopSleepInBed(true, true);
            }

            double x = this.enderman.getX() + Mth.nextInt(this.enderman.getRandom(), -3, 3);
            double z = this.enderman.getZ() + Mth.nextInt(this.enderman.getRandom(), -3, 3);
            double y = this.enderman.getY();

            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
            this.enderman.level().playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.HOSTILE, 1f, 0.5f);
        }
        else {
            this.target.setPos(this.enderman.getX(), this.enderman.getY(), this.enderman.getZ());
            this.target.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1f, 0.5f);
        }
        this.awayFromTargetTick = 0;
    }
}
