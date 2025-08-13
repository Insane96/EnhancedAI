package insane96mcp.enhancedai.modules.witch.darkart;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WITCH, description = "Witches summon Villagers and cast a lightning upon them.")
public class DarkArtWitch extends Feature {
    @Config(min = 0d, max = 1d, description = "Chance for a witch to get the Dark Art AI (as soon as they have a target and are less than 10 blocks away from the target will summon a Villager and cast a lightning bolt on them")
    public static Double chance = 0.333d;
	@Config(min = 0d, description = "At which distance from the villager will the witch will cancel the summoning")
	public static Integer cancelDistance = 16;

	public static ResourceLocation PERFORMING_DARK_ARTS;
	public static EAIData<Boolean> DARK_ARTS;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		PERFORMING_DARK_ARTS = this.createDataKey("performing_dark_arts");
		DARK_ARTS = EAIData.ofBool(this.createDataKey("dark_arts"), (witch, darkArt) -> {
			GoalHelper.removeGoal(witch.goalSelector, DarkArtWitchGoal.class);
			if (darkArt)
				witch.goalSelector.addGoal(1, new DarkArtWitchGoal(witch));
		});
    }

    //Lowest priority so other mods can set persistent data
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Witch witch))
            return;

		DARK_ARTS.applyIfAbsent(witch, witch.getRandom().nextDouble() < chance);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof Witch witch))
            return;

		GoalHelper.getGoal(witch.goalSelector, DarkArtWitchGoal.class)
				.ifPresent(DarkArtWitchGoal::forceStop);
    }
}
