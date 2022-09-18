package insane96mcp.enhancedai.modules.witch.feature;

import insane96mcp.enhancedai.modules.witch.ai.DarkArtWitchGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Dark Arts Witches", description = "Witches summon Villagers and cast a lightning bolt upon them.")
public class DarkArtWitch extends Feature {

    private final ForgeConfigSpec.DoubleValue darkArtChanceConfig;

    public double darkArtChance = 0.333d;

    public DarkArtWitch(Module module) {
        super(Config.builder, module);
        super.pushConfig(Config.builder);
        this.darkArtChanceConfig = Config.builder
                .comment("Chance for a witch to get the Dark Art AI (as soon as they have a target and are less than 10 blocks away from the target will summon a Villager and cast a lightning bolt on them")
                .defineInRange("Dark Art Chance", this.darkArtChance, 0d, 1d);
        Config.builder.pop();
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.darkArtChance = this.darkArtChanceConfig.get();
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinWorldEvent event) {
        if (!this.isEnabled()
                || event.getWorld().isClientSide
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
