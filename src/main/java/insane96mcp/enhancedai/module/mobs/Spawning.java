package insane96mcp.enhancedai.module.mobs;

import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = EAIModules.MOBS)
public class Spawning extends Feature {

	@Config(min = 0, max = 128, description = "How far away from any player monsters will instantly despawn? Vanilla is 128. Reducing this makes mobs more crowded around players.")
	public static Integer monstersDespawningDistance = 112;
	@Config(min = 0, max = 128, description = "How far away from any player monsters will be able to randomly despawn? Vanilla is 32")
	public static Integer minMonstersDespawningDistance = 48;

    public static ResourceLocation UNAFFECTED_BY_FEATURES;

    @Config(name = "Blacklisted spawn types", description = "Spawn types in this list will not get AI changes. Valid values: NATURAL, CHUNK_GENERATION, SPAWNER, STRUCTURE, BREEDING, MOB_SUMMONED, JOCKEY, EVENT, CONVERSION, REINFORCEMENT, TRIGGERED, BUCKET, SPAWN_EGG, COMMAND, DISPENSER, PATROL.")
    public static List<String> blacklistedSpawnTypesConfig = new ArrayList<>();
    private static final List<MobSpawnType> blacklistedSpawnTypes = new ArrayList<>();

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        UNAFFECTED_BY_FEATURES = this.createDataKey("unaffected_by_features");
    }

    @Override
    public void loadConfigOptions() {
        super.loadConfigOptions();
        blacklistedSpawnTypes.clear();
        blacklistedSpawnTypes.addAll(blacklistedSpawnTypesConfig.stream().map(MobSpawnType::valueOf).toList());
    }

    @Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		if (this.isEnabled())
			MobCategory.MONSTER.despawnDistance = monstersDespawningDistance;
	}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof LivingEntity living))
            return;

        if (isSpawnTypeBlacklisted(living))
            ModNBTData.put(living, UNAFFECTED_BY_FEATURES, true);
    }

    public static boolean isSpawnTypeBlacklisted(LivingEntity living) {
        if (!(living instanceof Mob mob))
            return false;
        for (MobSpawnType spawnType : blacklistedSpawnTypes) {
            if (mob.getSpawnType() == spawnType)
                return true;
        }
        return false;
    }

    public static boolean isUnaffectedByFeatures(Entity entity) {
        return ModNBTData.get(entity, UNAFFECTED_BY_FEATURES, Boolean.class);
    }
}
