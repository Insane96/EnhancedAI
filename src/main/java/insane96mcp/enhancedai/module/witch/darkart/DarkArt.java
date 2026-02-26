package insane96mcp.enhancedai.module.witch.darkart;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@LoadFeature(module = EAIModules.WITCH, description = "Witches summon Villagers and cast a lightning upon them. Only entity types in the `enhancedai:witch/can_perform_dark_arts` tag will be affected (can be used for any mob, not only witches).")
public class DarkArt extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("witch/can_perform_dark_arts"));
    @Config(min = 0d, max = 1d, description = "Chance for a witch to get the Dark Art AI (as soon as they have a target and are less than 10 blocks away from the target will summon a Villager and cast a lightning bolt on them")
    public static Double chance = 0.333d;
	@Config(min = 0d, description = "At which distance from the witch will she start performing the Dark Art")
	public static Integer triggerDistance = 10;
	@Config(min = 0d, description = "At which distance from the villager will the witch will cancel the summoning")
	public static Integer cancelDistance = 16;
	@Config(min = 0d)
	public static Boolean summonedWitchesCanBeDarkArt = false;

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
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		DARK_ARTS.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || !(event.getEntity() instanceof Mob mob))
            return;

		GoalHelper.getGoal(mob.goalSelector, DarkArtWitchGoal.class)
				.ifPresent(DarkArtWitchGoal::forceStop);
    }
}
