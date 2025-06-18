package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.mpr.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EASounds;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.module.base.TagsFeature;
import insane96mcp.insanelib.network.message.MessageCreeperDataSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@LoadFeature(module = Modules.Ids.CREEPER, description = "Various changes to Creepers exploding. Ignoring Walls, Walking Fuse and smarter exploding based off explosion size. Only creepers in the enhancedai:change_creeper_swell entity type tag are affected by this feature.")
public class CreeperSwell extends Feature {
	public static final TagKey<EntityType<?>> CHANGE_CREEPER_SWELL = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("change_creeper_swell"));
	public static final TagKey<EntityType<?>> CAN_CREEPER_LAUNCH = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_creeper_launch"));

	public static EAIData<Boolean> WALKING_FUSE;
	public static EAIData<Double> WALKING_FUSE_SPEED_MODIFIER;
	public static EAIData<Boolean> IGNORE_WALLS;
	public static EAIData<Boolean> BREACH;
	public static EAIData<Double> BREACH_HORIZONTAL_RANGE;
	public static EAIData<Boolean> BETA;
	public static EAIData<Boolean> BETA_LEFT_STRAFE;
	public static EAIData<Boolean> DISABLE_FALLING_SWELLING;
	public static EAIData<Boolean> TNT_LIKE;
	public static EAIData<Boolean> BLOW_UP_ON_DEATH;
	public static EAIData<Boolean> FORCE_EXPLODE;
	public static EAIData<Boolean> ANGRY;
	public static EAIData<Boolean> LAUNCH;
	public static EAIData<Double> LAUNCH_INACCURACY;
	public static EAIData<Boolean> LAUNCH_PARTICLES;
	public static EAIData<String> EXPLOSION_SOUND;

	@Config(min = 0d, max = 1d, description = "Percentage chance for a Creeper to keep walking while exploding. This is overwritten if the creeper has the beta property.")
	public static Double walkingFuse$chance = 0.1d;
	@Config(min = -1d, max = 64d, description = "Speed modifier when a walking fuse creeper is swelling.")
	public static Double walkingFuse$speedModifier = -0.5d;
	@Config(min = 0d, max = 1d, description = "Percentage chance for a Creeper to ignore walls while targeting a player. This means that a creeper will be able to explode if it's in the correct range from a player even if there's a wall between.")
	public static Double ignoreWallsChance = 0.65d;
	@Config(min = 0d, max = 1d, description = "Launching creepers will try ignite and throw themselves at the player.")
	public static Double launch$chance = 0.05d;
	@Config(description = "If true, Launching Creepers emit particles")
	public static Boolean launch$particles = true;
	@Config(min = 0d, max = 8d, description = "The inaccuracy of the launching creeper in Normal difficulty, easy is increased, hard is decreased.")
	public static Difficulty launch$inaccuracy = new Difficulty(2, 2, 1);
	@Config(min = 0, max = 127, description = "The explosion radius of launching creepers. Set to 0 to not change. (Overrides Cena creepers explosion radius)")
	public static Integer launch$explosionRadius = 2;
	@Config(min = 0d, max = 1d, description = "Breaching creepers will try to open a hole in the wall to let mobs in.")
	public static Double breach$chance = 0.075d;
	@Config(min = 0, description = "How far away (horizontally) from the target breaching creepers can breach.")
	public static Integer breach$horizontalRange = 24;
	@Config(min = 0d, max = 1d, description = "Beta creepers when exploding will walk around the target, like the creepers in pre-1.2. This takes precedence over walking fuse.")
	public static Double beta$chance = 0.35d;
	@Config(description = "Disables the creeper feature that makes them start swelling when falling.")
	public static Boolean disableFallingSwelling = true;

	@Config(description = "If true creepers will ignite if damaged by an explosion.")
	public static Boolean tntLike = false;
	//Angry
	@Config(min = 0d, max = 1d, description = "Chance for a creeper to spawn angry")
	public static Double angry$chance = 0.03d;
	@Config(description = "If true, Angry Creeper emits particles")
	public static Boolean angry$particles = true;
	@Config(description = "The special sound effect that the Angry Creeper plays")
	public static FuseExplodeSounds angry$sounds = FuseExplodeSounds.OLD;
	@Config(description = "If true, Angry Creeper will have a name")
	public static Boolean angry$name = true;
	@Config(description = "When ignited, Angry Creeper will not stop swelling")
	public static Boolean angry$forceExplosion = false;
	@Config(description = "Makes angry creepers blow up on death like when they were added back in 0.30")
	public static Boolean angry$explodeOnDeath = true;
	@Config(description = "If true, Angry Creeper explosion will generate fire")
	public static Boolean angry$fire = false;
	@Config(min = 0d, max = 12d, description = "Explosion power of Angry Creeper")
	public static Double angry$explosionPower = 4d;
	@Config(description = "Makes creepers blow up on death like when they were added back in 0.30")
	public static BlowUpOnDeath blowUpOnDeath = BlowUpOnDeath.CHARGED;
	@Config(description = "If Insane's Survival Overhaul is installed and Explosion Overhaul feature is enabled, Angry creeper will deal more knockback and break more blocks, breaching creepers will break more blocks")
	public static Boolean insaneSurvivalOverhaulIntegration = true;

	public CreeperSwell(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		WALKING_FUSE = EAIData.ofBool(this.createDataKey("walking_fuse"), (mob, walkingFuse) -> {
			GoalHelper.getGoal(mob, EACreeperSwellGoal.class).ifPresent(eaCreeperSwellGoal -> eaCreeperSwellGoal.setWalkingFuse(walkingFuse));
		});
		WALKING_FUSE_SPEED_MODIFIER = EAIData.ofDouble(this.createDataKey("walking_fuse_speed_modifier"));
		IGNORE_WALLS = EAIData.ofBool(this.createDataKey("ignore_walls"));
		BREACH = EAIData.ofBool(this.createDataKey("breach"));
		BREACH_HORIZONTAL_RANGE = EAIData.ofDouble(this.createDataKey("breach_horizontal_range"));
		BETA = EAIData.ofBool(this.createDataKey("beta"), (mob, beta) -> {
			GoalHelper.getGoal(mob, EACreeperSwellGoal.class).ifPresent(eaCreeperSwellGoal -> eaCreeperSwellGoal.setBeta(beta));
		});
		BETA_LEFT_STRAFE = EAIData.ofBool(this.createDataKey("beta_left_strafe"));
		DISABLE_FALLING_SWELLING = EAIData.ofBool(this.createDataKey("disable_falling_swelling"));
		TNT_LIKE = EAIData.ofBool(this.createDataKey("tnt_like"));
		BLOW_UP_ON_DEATH = EAIData.ofBool(this.createDataKey("blow_up_on_death"));
		FORCE_EXPLODE = EAIData.ofBool(this.createDataKey("force_explode"));
		ANGRY = EAIData.ofBool(this.createDataKey("angry"), (mob, angry) -> {
			if (!(mob instanceof Creeper creeper))
				return;
			CompoundTag compoundNBT = new CompoundTag();
			creeper.addAdditionalSaveData(compoundNBT);
			if (angry) {
				compoundNBT.putShort("Fuse", (short) 36);
				compoundNBT.putByte("ExplosionRadius", angry$explosionPower.byteValue());
				if (angry$name)
					creeper.setCustomName(Component.literal("Angry Creeper"));
				if (angry$fire)
					TagsFeature.setExplosionCausesFire(true, creeper);
				if (insaneSurvivalOverhaulIntegration) {
					creeper.getPersistentData().putFloat("iguanatweaksreborn:explosion_knockback_multiplier", 2f);
					creeper.getPersistentData().putFloat("iguanatweaksreborn:explosion_ray_strength_multiplier", 0.01f);
				}
				if (angry$forceExplosion)
					FORCE_EXPLODE.apply(creeper, true);
				EXPLOSION_SOUND.apply(creeper, angry$sounds.name);
			}
			else {
				compoundNBT.putShort("Fuse", (short) 30);
				compoundNBT.putByte("ExplosionRadius", (byte) 3);
				if (angry$name)
					creeper.setCustomName(null);
				if (angry$fire)
					TagsFeature.setExplosionCausesFire(false, creeper);
				if (insaneSurvivalOverhaulIntegration) {
					creeper.getPersistentData().remove("iguanatweaksreborn:explosion_knockback_multiplier");
					creeper.getPersistentData().remove("iguanatweaksreborn:explosion_ray_strength_multiplier");
				}
				if (angry$forceExplosion)
					FORCE_EXPLODE.apply(creeper, false);
				EXPLOSION_SOUND.apply(creeper, FuseExplodeSounds.NONE.name);
			}
			creeper.readAdditionalSaveData(compoundNBT);
			MessageCreeperDataSync.syncCreeperToPlayers(creeper);
		});
		LAUNCH = EAIData.ofBool(this.createDataKey("launch"), (mob, launch) -> {
			if (!(mob instanceof Creeper creeper))
				return;
			CompoundTag compoundNBT = new CompoundTag();
			creeper.addAdditionalSaveData(compoundNBT);
			if (launch) {
				mob.goalSelector.addGoal(1, new EACreeperLaunchGoal(creeper));
				if (launch$explosionRadius > 0)
					compoundNBT.putByte("ExplosionRadius", launch$explosionRadius.byteValue());
			}
			else {
				if (mob.goalSelector.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof EACreeperLaunchGoal) && launch$explosionRadius > 0)
					compoundNBT.putByte("ExplosionRadius", (byte) 3);
			}
			creeper.readAdditionalSaveData(compoundNBT);
			MessageCreeperDataSync.syncCreeperToPlayers(creeper);
		});
		LAUNCH_INACCURACY = EAIData.ofDouble(this.createDataKey("launch_inaccuracy"));
		LAUNCH_PARTICLES = EAIData.ofBool(this.createDataKey("launch_particles"));
		EXPLOSION_SOUND = EAIData.ofString(this.createDataKey("explosion_sound"));
	}

	@SubscribeEvent
	public void explosionStartEvent(ExplosionEvent.Detonate event) {
		if (!this.isEnabled())
			return;

		Explosion e = event.getExplosion();

		if (!(e.getExploder() instanceof LivingEntity living))
			return;

		FuseExplodeSounds fuseExplodeSounds = FuseExplodeSounds.get(living);
		if (fuseExplodeSounds != FuseExplodeSounds.NONE)
            //noinspection DataFlowIssue
            living.playSound(fuseExplodeSounds.explode.get(), 4.0f, 1f);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.getType().is(CHANGE_CREEPER_SWELL))
			return;

		if (!creeper.goalSelector.availableGoals.removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof SwellGoal))
			return;

		EACreeperSwellGoal swellGoal = new EACreeperSwellGoal(creeper);
		creeper.goalSelector.addGoal(2, swellGoal);
		WALKING_FUSE.apply(creeper, creeper.getRandom().nextDouble() < walkingFuse$chance);
		WALKING_FUSE_SPEED_MODIFIER.apply(creeper, walkingFuse$speedModifier);
		IGNORE_WALLS.apply(creeper, creeper.getRandom().nextDouble() < ignoreWallsChance);
		BREACH.apply(creeper, creeper.getRandom().nextDouble() < breach$chance);
		BREACH_HORIZONTAL_RANGE.apply(creeper, breach$horizontalRange.doubleValue());
		BETA.apply(creeper, creeper.getRandom().nextDouble() < beta$chance);
		DISABLE_FALLING_SWELLING.apply(creeper, disableFallingSwelling);
		TNT_LIKE.apply(creeper, tntLike);
		BLOW_UP_ON_DEATH.apply(creeper, blowUpOnDeath == BlowUpOnDeath.ALL || (blowUpOnDeath == BlowUpOnDeath.CHARGED && creeper.isPowered()) || (ANGRY.get(creeper) && angry$explodeOnDeath));
		ANGRY.apply(creeper, creeper.getRandom().nextDouble() < angry$chance);
		LAUNCH.apply(creeper, creeper.getRandom().nextDouble() < launch$chance && creeper.getType().is(CAN_CREEPER_LAUNCH));
		LAUNCH_INACCURACY.apply(creeper, launch$inaccuracy.getByDifficulty(creeper.level()));
		LAUNCH_PARTICLES.apply(creeper, launch$particles);
	}

	@SubscribeEvent
	public void livingDamageEvent(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !event.getSource().is(DamageTypeTags.IS_EXPLOSION)
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !TNT_LIKE.get(creeper))
			return;

		creeper.ignite();
	}

	@SubscribeEvent
	public void onCreeperRemoved(EntityLeaveLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.isDeadOrDying()
				|| creeper.level().isClientSide
				|| !BLOW_UP_ON_DEATH.get(creeper))
			return;

		BLOW_UP_ON_DEATH.apply(creeper, false);
		creeper.explodeCreeper();
	}

	@SubscribeEvent
	public void onCreeperTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Creeper creeper)
				|| creeper.level().isClientSide)
			return;

		onLaunchCreeperTick(creeper);
		onCenaCreeperTick(creeper);
	}

	public void onLaunchCreeperTick(Creeper creeper) {
		if (creeper.tickCount % 20 != 0
				|| !LAUNCH_PARTICLES.get(creeper)
				|| !LAUNCH.get(creeper))
			return;
		ServerLevel serverLevel = (ServerLevel) creeper.level();
		serverLevel.players().forEach(player -> serverLevel.sendParticles(player, ParticleTypes.CLOUD, true, creeper.getX(), creeper.getY() + 0.25d, creeper.getZ(), 8, 0.05, 0.05, 0.05, 0.025));
	}

	public void onCenaCreeperTick(Creeper creeper) {
		if (creeper.tickCount % 40 != 5
				|| !angry$particles)
			return;
		ServerLevel serverLevel = (ServerLevel) creeper.level();
		if (ANGRY.get(creeper)) {
			for (int j = 0; j < serverLevel.players().size(); ++j) {
				ServerPlayer serverplayer = serverLevel.players().get(j);
				BlockPos blockpos = serverplayer.blockPosition();
				if (!blockpos.closerToCenterThan(new Vec3(creeper.getX(), creeper.getY() + 0.5d, creeper.getZ()), 16))
					continue;
				serverLevel.sendParticles(serverplayer, ParticleTypes.ANGRY_VILLAGER, false, creeper.getX(), creeper.getY() + 1.1d, creeper.getZ(), 1, 0.15, 0.15, 0.15, 0);
			}
		}
	}

	public enum FuseExplodeSounds {
		NONE("none", null, null),
		CENA("cena", EASounds.CREEPER_CENA_FUSE, EASounds.CREEPER_CENA_EXPLODE),
		WTF_BOOM("wtf_boom", EASounds.WTF_BOOM_FUSE, EASounds.WTF_BOOM_EXPLODE),
		OLD("old", () -> SoundEvents.CREEPER_PRIMED, EASounds.OLD_EXPLODE);

		public final String name;
		@Nullable
		public final Supplier<SoundEvent> fuse;
		@Nullable
		public final Supplier<SoundEvent> explode;

		FuseExplodeSounds(String name, @Nullable Supplier<SoundEvent> fuse, @Nullable Supplier<SoundEvent> explode) {
			this.name = name;
			this.fuse = fuse;
			this.explode = explode;
		}

		public static FuseExplodeSounds get(LivingEntity living) {
			String sound = CreeperSwell.EXPLOSION_SOUND.get(living);
			if (sound.isEmpty())
				return NONE;
			for (FuseExplodeSounds fuseExplodeSounds : values()) {
				if (fuseExplodeSounds.name.equals(sound))
					return fuseExplodeSounds;
			}
			return NONE;
		}
	}

	public enum BlowUpOnDeath {
		NONE,
		CHARGED,
		ALL
	}
}
