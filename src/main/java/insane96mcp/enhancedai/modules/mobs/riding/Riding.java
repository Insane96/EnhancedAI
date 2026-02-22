package insane96mcp.enhancedai.modules.mobs.riding;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Makes mobs ride other mobs. Mobs in the `enhancedai:mobs/riding/can_be_mounted` tag will be able to be mounted, while mobs in the `enhancedai:mobs/riding/can_mount` tag will be able to mount other mobs.")
public class Riding extends Feature {
    public static final TagKey<EntityType<?>> CAN_BE_MOUNTED = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/riding/can_be_mounted"));
    public static final TagKey<EntityType<?>> CAN_MOUNT = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/riding/can_mount"));
    @Config(min = 0d, max = 1d, description = "Chance for a mob to have an AI to go and ride mobs.")
    public static DifficultyBasedConfig chance = new DifficultyBasedConfig(0.03d, 0.06d, 0.1d);

    @Config(description = "If true, riding mobs will dismount if take too much suffocation damage.")
    public static Boolean stopMountingIfSuffocating = true;

	public static ResourceLocation SUFFOCATION_WHILE_RIDING;
	public static EAIData<String> CAN_MOUNT_DATA;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		SUFFOCATION_WHILE_RIDING = this.createDataKey("suffocation_while_riding");
		CAN_MOUNT_DATA = EAIData.ofString(this.createDataKey("can_mount"));
	}

	@SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_MOUNT))
            return;

		if (mob.getRandom().nextDouble() < chance.getByDifficulty(mob.level()))
			CAN_MOUNT_DATA.applyIfAbsent(mob, CAN_BE_MOUNTED.location().toString());
		mob.targetSelector.addGoal(1, new SearchMountGoal(mob));
    }

    @SubscribeEvent
    public void onDamageTaken(LivingDamageEvent.Pre event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !stopMountingIfSuffocating
                || !event.getSource().is(DamageTypes.IN_WALL)
                || event.getEntity().getVehicle() == null)
            return;

        float suffocatingDamageTaken = ModNBTData.get(mob, SUFFOCATION_WHILE_RIDING, Float.class);
        suffocatingDamageTaken += event.getNewDamage();
        if (suffocatingDamageTaken >= 6f) {
			mob.stopRiding();
            ModNBTData.remove(mob, SUFFOCATION_WHILE_RIDING);
        }
        else
            ModNBTData.put(mob, SUFFOCATION_WHILE_RIDING, suffocatingDamageTaken);
    }
}
