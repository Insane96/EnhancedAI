package insane96mcp.enhancedai.modules.shulker.shulkerattack;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SHULKER)
public class ShulkerAttack extends Feature {
    @Config(min = 0, description = "Ticks before the first bullet is fired")
    public static MinMax baseAttackSpeed = new MinMax(20, 40);
    @Config(min = 1, description = "Ticks to fire is calculated as base_attack_speed + (0~extra_attack_speed)")
    public static MinMax extraAttackSpeed = new MinMax(100, 200);

    public static EAIData<Integer> BASE_ATTACK_SPEED;
    public static EAIData<Integer> EXTRA_ATTACK_SPEED;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        BASE_ATTACK_SPEED = EAIData.ofInt(this.createDataKey("base_attack_speed"));
        EXTRA_ATTACK_SPEED = EAIData.ofInt(this.createDataKey("extra_attack_speed"));
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Shulker shulker)) return;

        GoalHelper.removeGoal(shulker.goalSelector, Shulker.ShulkerAttackGoal.class);

        EAIShulkerAttackGoal attackGoal = new EAIShulkerAttackGoal(shulker);
        shulker.goalSelector.addGoal(2, attackGoal);
        BASE_ATTACK_SPEED.applyIfAbsent(shulker, baseAttackSpeed.getIntRandBetween(shulker.getRandom()));
        EXTRA_ATTACK_SPEED.applyIfAbsent(shulker, extraAttackSpeed.getIntRandBetween(shulker.getRandom()));
    }
}