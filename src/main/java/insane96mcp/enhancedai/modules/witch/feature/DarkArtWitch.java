package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.witch.ai.DarkArtWitchGoal;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Dark Arts Witches", description = "Witches summon Villagers and cast a lightning upon them.")
@LoadFeature(module = Modules.Ids.WITCH)
public class DarkArtWitch extends Feature {
    @Config(min = 0d, max = 1d)
    @Label(name = "Dark Art Chance", description = "Chance for a witch to get the Dark Art AI (as soon as they have a target and are less than 10 blocks away from the target will summon a Villager and cast a lightning bolt on them")
    public static Double darkArtChance = 0.333d;

    public DarkArtWitch(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Witch witch))
            return;

        CompoundTag persistentData = witch.getPersistentData();
        boolean darkArt = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Witch.DARK_ARTS, witch.level.random.nextDouble() < this.darkArtChance);

        if (!darkArt)
            return;

        DarkArtWitchGoal darkArtWitchGoal = new DarkArtWitchGoal(witch);
        witch.goalSelector.addGoal(1, darkArtWitchGoal);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level.isClientSide
                || !(event.getEntity() instanceof Witch witch))
            return;


        witch.goalSelector.availableGoals.forEach(prioritizedGoal -> {
            if (prioritizedGoal.getGoal() instanceof DarkArtWitchGoal goal) {
                if (goal.isRunning()) {
                    goal.forceStop();
                }
            }
        });
    }
}
