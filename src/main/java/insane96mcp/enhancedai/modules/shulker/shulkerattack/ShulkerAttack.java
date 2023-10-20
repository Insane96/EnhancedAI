package insane96mcp.enhancedai.modules.shulker.shulkerattack;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Shulker Attack")
@LoadFeature(module = Modules.Ids.SHULKER)
public class ShulkerAttack extends Feature {
    public static final String BASE_ATTACK_SPEED = EnhancedAI.RESOURCE_PREFIX + "base_attack_speed";
    public static final String ATTACK_SPEED_BONUS_HALF_SECONDS = EnhancedAI.RESOURCE_PREFIX + "attack_speed_bonus_half_seconds";
    @Config(min = 1, max = 40)
    @Label(name = "Base Attack Speed", description = "Ticks before the first bullet is fired")
    public static MinMax baseAttackSpeed = new MinMax(20, 40);
    @Config(min = 1, max = 40)
    @Label(name = "Attack speed bonus half seconds", description = "Ticks to fire is calculcated as base_attack_speed + (0~attack_speed_bonus_half_seconds * 10)")
    public static MinMax attackSpeedBonusHalfSeconds = new MinMax(10, 20);

    public ShulkerAttack(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Shulker shulker)) return;

        boolean hasAttackGoal = false;
        //Remove Shulker Swell Goal
        ArrayList<Goal> goalsToRemove = new ArrayList<>();
        for (WrappedGoal prioritizedGoal : shulker.goalSelector.availableGoals) {
            if (prioritizedGoal.getGoal() instanceof Shulker.ShulkerAttackGoal) {
                goalsToRemove.add(prioritizedGoal.getGoal());
                hasAttackGoal = true;
            }
        }

        if (!hasAttackGoal)
            return;

        goalsToRemove.forEach(shulker.goalSelector::removeGoal);

        CompoundTag persistentData = shulker.getPersistentData();

        int baseAttackSpeed1 = NBTUtils.getIntOrPutDefault(persistentData, BASE_ATTACK_SPEED, baseAttackSpeed.getIntRandBetween(shulker.getRandom()));
        int attackSpeedBonusHalfSeconds1 = NBTUtils.getIntOrPutDefault(persistentData, ATTACK_SPEED_BONUS_HALF_SECONDS, attackSpeedBonusHalfSeconds.getIntRandBetween(shulker.getRandom()));

        EAShulkerAttackGoal attackGoal = new EAShulkerAttackGoal(shulker, baseAttackSpeed1, attackSpeedBonusHalfSeconds1);
        shulker.goalSelector.addGoal(2, attackGoal);
    }
}