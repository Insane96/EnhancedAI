package insane96mcp.enhancedai.modules.villager.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Villager Attacking", description = "Make villagers fight back")
@LoadFeature(module = Modules.Ids.VILLAGER)
public class VillagerAttacking extends Feature {
    //TODO add attack back on low reputation only
    @Config
    @Label(name = "Villagers Fight back", description = "If true, when attacked, villagers will call other villagers for help and attack back. Attack damage can't be changed via config due to limitation so use mods like Mobs Properties Randomness to change the damage. Base damage is 4")
    public static Boolean villagersFightBack = true;
    @Config(min = 0d, max = 4d)
    @Label(name = "Movement Speed Multiplier", description = "Movement speed multiplier when attacking")
    public static Double speedMultiplier = 0.8d;
    @Config
    @Label(name = "Entity Blacklist", description = "Entities that shouldn't be affected by this feature")
    public static Blacklist entityBlacklist = new Blacklist(List.of(), false);

    public VillagerAttacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Villager villager)
                || entityBlacklist.isEntityBlackOrNotWhitelist(villager))
            return;

        CompoundTag persistentData = villager.getPersistentData();

        double movementSpeedMultiplier = NBTUtils.getDoubleOrPutDefault(persistentData, EAStrings.Tags.Passive.SPEED_MULTIPLIER_WHEN_AGGROED, speedMultiplier);

        if (villagersFightBack) {
            villager.targetSelector.addGoal(1, (new HurtByTargetGoal(villager)).setAlertOthers());
            villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, movementSpeedMultiplier, false));
            //villager.getBrain().removeAllBehaviors();
        }
    }
}