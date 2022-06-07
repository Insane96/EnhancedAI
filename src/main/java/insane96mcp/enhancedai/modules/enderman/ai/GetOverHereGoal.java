package insane96mcp.enhancedai.modules.enderman.ai;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

        if (this.enderMan.getTarget().distanceTo(this.enderMan) > 8d) {
            this.awayFromTargetTick++;
        }
        else {
            this.awayFromTargetTick = 0;
        }

        return this.awayFromTargetTick > reducedTickDelay(100);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void start() {
        this.target = this.enderMan.getTarget();
        this.target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20));
        if (this.target instanceof ServerPlayer player) {
            if (player.isSleeping()) {
                player.stopSleepInBed(true, true);
            }

            player.connection.teleport(this.enderMan.getX(), this.enderMan.getY(), this.enderMan.getZ(), player.getXRot(), player.getYRot());
            player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1f, 0.5f);
        }
        else {
            this.target.setPos(this.enderMan.getX(), this.enderMan.getY(), this.enderMan.getZ());
            this.target.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1f, 0.5f);
        }
        this.awayFromTargetTick = 0;
    }
}
