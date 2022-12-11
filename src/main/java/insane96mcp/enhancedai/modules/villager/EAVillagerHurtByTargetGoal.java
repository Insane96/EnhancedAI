package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.modules.villager.feature.VillagerAttacking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class EAVillagerHurtByTargetGoal extends HurtByTargetGoal {

    Villager villager;

    public EAVillagerHurtByTargetGoal(Villager villager, Class<?>... toIgnoreDamage) {
        super(villager, toIgnoreDamage);
        this.villager = villager;
    }

    @Override
    public boolean canUse() {
        LivingEntity hurtVillager = this.mob.getLastHurtByMob();
        if (hurtVillager instanceof Player player) {
            return villager.getPlayerReputation(player) <= VillagerAttacking.minReputationFightBack && super.canUse();
        }
        else if (hurtVillager instanceof Enemy) {
            return VillagerAttacking.villagersFightBackEnemies && super.canUse();
        }
        return super.canUse();
    }
}
