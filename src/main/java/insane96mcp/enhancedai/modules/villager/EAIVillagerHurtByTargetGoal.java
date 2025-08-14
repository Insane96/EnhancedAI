package insane96mcp.enhancedai.modules.villager;

import insane96mcp.enhancedai.ai.EAIHurtByTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class EAIVillagerHurtByTargetGoal extends EAIHurtByTargetGoal {

    Villager villager;

    public EAIVillagerHurtByTargetGoal(Villager villager, Class<?>... toIgnoreDamage) {
        super(villager, toIgnoreDamage);
        this.villager = villager;
    }

    @Override
    public boolean canUse() {
		if (!super.canUse())
			return false;
        LivingEntity hurtVillager = this.mob.getLastHurtByMob();
        if (hurtVillager instanceof Player player)
            return villager.getPlayerReputation(player) <= VillagerAttacking.ATTACK_BELOW_REPUTATION.get(this.villager);
        else if (hurtVillager instanceof Enemy)
            return VillagerAttacking.FIGHTS_BACK_ENEMIES.get(this.villager);
        return true;
    }
}
