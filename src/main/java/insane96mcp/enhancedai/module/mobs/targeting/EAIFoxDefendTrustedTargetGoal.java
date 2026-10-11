package insane96mcp.enhancedai.module.mobs.targeting;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Fox;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Foxes target whoever hurt the entities they trust
 */
public class EAIFoxDefendTrustedTargetGoal extends EAINearestAttackableTarget<LivingEntity> {
    @Nullable
    private LivingEntity trustedLastHurtBy;
    @Nullable
    private LivingEntity trustedLastHurt;
    private int timestamp;

    public EAIFoxDefendTrustedTargetGoal(Fox goalOwnerIn, boolean nearbyOnlyIn, TargetingConditions targetingConditions) {
        super(goalOwnerIn, LivingEntity.class, nearbyOnlyIn, targetingConditions);
    }

    public boolean canUse() {
        int targetChance = Targeting.TARGET_CHANCE.get(this.mob);
        if (targetChance > 0 && this.mob.getRandom().nextInt(targetChance) != 0)
            return false;
        for (UUID uuid : ((Fox) this.mob).getTrustedUUIDs()) {
            if (uuid != null
                    && this.mob.level() instanceof ServerLevel serverLevel
                    && serverLevel.getEntity(uuid) instanceof LivingEntity trusted) {
                this.trustedLastHurt = trusted;
                this.trustedLastHurtBy = trusted.getLastHurtByMob();
                this.targetEntitySelector.range(this.getFollowDistance());
                return trusted.getLastHurtByMobTimestamp() != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetEntitySelector);
            }
        }
        return false;
    }

    public void start() {
        Fox fox = (Fox) this.mob;
        this.nearestTarget = this.trustedLastHurtBy;
        if (this.trustedLastHurt != null)
            this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
        fox.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
        fox.setDefending(true);
        fox.wakeUp();
        super.start();
    }
}
