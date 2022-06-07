package insane96mcp.enhancedai.modules.enderman.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;

public class GetOverHereGoal extends Goal {

    private final EnderMan enderMan;
    private LivingEntity target;

    private int awayFromTargetTick = 0;

    public GetOverHereGoal(EnderMan enderMan) {
        this.enderMan = enderMan;
    }

    @Override
    public boolean canUse() {
        if (this.enderMan.getTarget() == null)
            return false;

        if (this.enderMan.getTarget().distanceToSqr(this.enderMan) > 8d) {
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
        this.target = this.enderMan.getTarget();
        if (this.target instanceof ServerPlayer player) {
            if (player.isSleeping()) {
                player.stopSleepInBed(true, true);
            }

            double x = this.enderMan.getX() + Mth.nextInt(this.enderMan.getRandom(), -3, 3);
            double z = this.enderMan.getZ() + Mth.nextInt(this.enderMan.getRandom(), -3, 3);
            double y = this.enderMan.getY();

            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
            this.enderMan.level.playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.HOSTILE, 1f, 0.5f);
        }
        else {
            this.target.setPos(this.enderMan.getX(), this.enderMan.getY(), this.enderMan.getZ());
            this.target.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1f, 0.5f);
        }
        this.awayFromTargetTick = 0;
    }
}
