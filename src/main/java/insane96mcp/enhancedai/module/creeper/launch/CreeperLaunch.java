package insane96mcp.enhancedai.module.creeper.launch;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = EAIModules.CREEPER, description	= "Creepers can ignite and throw themselves at players. Only entity types in the enhancedai:creeper/can_launch tag will be affected by this feature.")
public class CreeperLaunch extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("creeper/can_launch"));

	@Config(min = 0d, max = 1d, description = "Launching creepers will try ignite and throw themselves at the player. Only creepers in the enhancedai:creeper/can_launch")
	public static Double chance = 0.05d;
	@Config(description = "If true, Launching Creepers emit particles")
	public static Boolean particles = true;
	@Config(min = 0d, max = 8d, description = "The inaccuracy of the launching creeper in Normal difficulty, easy is increased, hard is decreased.")
	public static DifficultyBasedConfig inaccuracy = new DifficultyBasedConfig(2, 2, 1);
	@Config(min = 0, max = 127, description = "The explosion radius of launching creepers. Set to 0 to not change. (Overrides Cena creepers explosion radius)")
	public static Integer explosionRadius = 2;

	public static EAIData<Boolean> LAUNCH;
	public static EAIData<Double> INACCURACY;
	public static EAIData<Boolean> PARTICLES;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		LAUNCH = EAIData.ofBool(this.createDataKey("launch"), (mob, launch) -> {
			if (!(mob instanceof Creeper creeper))
				return;
			boolean wasLaunch = GoalHelper.removeGoal(creeper.goalSelector, EAICreeperLaunchGoal.class).isPresent();
			CompoundTag compoundNBT = new CompoundTag();
			creeper.addAdditionalSaveData(compoundNBT);
			if (launch) {
				mob.goalSelector.addGoal(1, new EAICreeperLaunchGoal(creeper));
				if (explosionRadius > 0)
					compoundNBT.putByte("ExplosionRadius", explosionRadius.byteValue());
			}
			else if (wasLaunch && explosionRadius > 0)
				compoundNBT.putByte("ExplosionRadius", (byte) 3);
			creeper.readAdditionalSaveData(compoundNBT);
		});
		INACCURACY = EAIData.ofDouble(this.createDataKey("inaccuracy"));
		PARTICLES = EAIData.ofBool(this.createDataKey("particles"));
	}

	@SubscribeEvent
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		LAUNCH.applyIfAbsent(creeper, creeper.getRandom().nextDouble() < chance);
		INACCURACY.applyIfAbsent(creeper, inaccuracy.getByDifficulty(creeper.level()));
		PARTICLES.applyIfAbsent(creeper, particles);
	}

	@SubscribeEvent
	public void onCreeperTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !(creeper.level() instanceof ServerLevel serverLevel)
				|| (creeper.tickCount + creeper.getId()) % 20 != 0
				|| !LAUNCH.get(creeper)
				|| !PARTICLES.get(creeper))
			return;

		serverLevel.sendParticles(ParticleTypes.CLOUD, creeper.getX(), creeper.getY() + 0.25d, creeper.getZ(), 8, 0.05, 0.05, 0.05, 0.025);
	}
}
