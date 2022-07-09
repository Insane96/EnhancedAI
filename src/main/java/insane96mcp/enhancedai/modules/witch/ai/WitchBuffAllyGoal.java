package insane96mcp.enhancedai.modules.witch.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.raid.Raider;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class WitchBuffAllyGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private static final int DEFAULT_COOLDOWN = 200;
    private int cooldown = 0;

    public WitchBuffAllyGoal(Mob mob, Class<T> targetClass, boolean mustSee, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, 500, mustSee, false, targetPredicate);
    }

    public boolean canUse() {
        if (this.cooldown <= 0 && this.mob.getRandom().nextBoolean()) {
            if (((Raider)this.mob).hasActiveRaid()) {
                return false;
            } else {
                this.findTarget();
                return this.target != null;
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.cooldown = reducedTickDelay(DEFAULT_COOLDOWN);
        super.start();
    }

    @Override
    public void tick() {
        this.cooldown--;
    }
}
