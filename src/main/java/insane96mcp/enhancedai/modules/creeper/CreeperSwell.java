package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.EASounds;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

@LoadFeature(module = Modules.Ids.CREEPER, description = "Various changes to Creepers exploding. Ignoring Walls, Walking Fuse and smarter exploding based off explosion size. Only creepers in the enhancedai:change_creeper_swell entity type tag are affected by this feature.")
public class CreeperSwell extends Feature {
	public static final TagKey<EntityType<?>> CHANGE_CREEPER_SWELL = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("change_creeper_swell"));
	public static final TagKey<EntityType<?>> CAN_CREEPER_LAUNCH = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_creeper_launch"));

	public static final String LAUNCH = EnhancedAI.RESOURCE_PREFIX + "launch";
	public static final String BREACH = EnhancedAI.RESOURCE_PREFIX + "breach";
	public static final String ANGRY = EnhancedAI.RESOURCE_PREFIX + "angry";
	public static final String WALKING_FUSE = EnhancedAI.RESOURCE_PREFIX + "walking_fuse";
	public static final String IGNORE_WALLS = EnhancedAI.RESOURCE_PREFIX + "ignore_walls";
	public static final String BETA = EnhancedAI.RESOURCE_PREFIX + "beta";

	@Config(min = 0d, max = 1d, description = "Percentage chance for a Creeper to keep walking while exploding. This is overwritten if the creeper has the beta property.")
	public static Double walkingFuseChance = 0.1d;
	@Config(min = -1d, max = 64d, description = "Speed modifier when a walking fuse creeper is swelling.")
	public static Double walkingFuseSpeedModifier = -0.5d;
	@Config(min = 0d, max = 1d, description = "Percentage chance for a Creeper to ignore walls while targeting a player. This means that a creeper will be able to explode if it's in the correct range from a player even if there's a wall between.")
	public static Double ignoreWallsChance = 0.65d;
	@Config(min = 0d, max = 1d, description = "Launching creepers will try ignite and throw themselves at the player.")
	public static Double launch$chance = 0.05d;
	@Config(description = "If true, Launching Creepers emit particles")
	public static Boolean launch$particles = true;
	@Config(min = 0d, max = 8d, description = "The inaccuracy of the launching creeper in Normal difficulty, easy is increased, hard is decreased.")
	public static Double launch$inaccuracy = 0.5d;
	@Config(min = 0, max = 127, description = "The explosion radius of launching creepers. Set to 0 to not change. (Overrides Cena creepers explosion radius)")
	public static Integer launch$explosionRadius = 2;
	@Config(min = 0d, max = 1d, description = "Breaching creepers will try to open a hole in the wall to let mobs in.")
	public static Double breach$chance = 0.075d;
	@Config(min = 0, description = "How far away (horizontally) from the target breaching creepers can breach.")
	public static Integer breach$horizontalRange = 24;
	@Config(min = 0d, max = 1d, description = "Beta creepers when exploding will walk around the target, like the creepers in pre-1.2.")
	public static Double betaCreeperChance = 0.35d;
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
	public static AngryCreeperSounds angry$creeperSounds = AngryCreeperSounds.OLD_EXPLOSION;
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
	}

	@SubscribeEvent
	public void explosionStartEvent(ExplosionEvent.Detonate event) {
		if (!this.isEnabled())
			return;

		Explosion e = event.getExplosion();

		if (!(e.getExploder() instanceof Creeper creeper))
			return;

		if (creeper.getPersistentData().getBoolean(ANGRY) && angry$creeperSounds.explode != null)
			creeper.playSound(angry$creeperSounds.explode.get(), 4.0f, 1.0f);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.getType().is(CHANGE_CREEPER_SWELL))
			return;

		boolean hasSwellGoal = false;
		//Remove Creeper Swell Goal
		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		for (WrappedGoal prioritizedGoal : creeper.goalSelector.availableGoals) {
			if (prioritizedGoal.getGoal() instanceof SwellGoal) {
				goalsToRemove.add(prioritizedGoal.getGoal());
				hasSwellGoal = true;
			}
		}

		if (!hasSwellGoal)
			return;

		goalsToRemove.forEach(creeper.goalSelector::removeGoal);

		CompoundTag persistentData = creeper.getPersistentData();

		boolean walkingFuse = NBTUtils.getBooleanOrPutDefault(persistentData, WALKING_FUSE, creeper.getRandom().nextDouble() < walkingFuseChance);
		boolean ignoreWalls = NBTUtils.getBooleanOrPutDefault(persistentData, IGNORE_WALLS, creeper.getRandom().nextDouble() < ignoreWallsChance);
		boolean breach = NBTUtils.getBooleanOrPutDefault(persistentData, BREACH, creeper.getRandom().nextDouble() < breach$chance);
		boolean launch = creeper.getType().is(CAN_CREEPER_LAUNCH) && NBTUtils.getBooleanOrPutDefault(persistentData, LAUNCH, creeper.getRandom().nextDouble() < launch$chance);
		boolean angry = NBTUtils.getBooleanOrPutDefault(persistentData, ANGRY, creeper.getRandom().nextDouble() < angry$chance);
		boolean beta = NBTUtils.getBooleanOrPutDefault(persistentData, BETA, creeper.getRandom().nextDouble() < betaCreeperChance);

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
				persistentData.putFloat("iguanatweaksreborn:explosion_knockback_multiplier", 2f);
				persistentData.putFloat("iguanatweaksreborn:explosion_ray_strength_multiplier", 0.01f);
			}
		}

		EACreeperSwellGoal swellGoal = new EACreeperSwellGoal(creeper)
				.setWalkingFuse(walkingFuse && !beta)
				.setIgnoreWalls(ignoreWalls)
				.setBreaching(breach)
				.setBeta(beta);
		if (angry && angry$forceExplosion)
			swellGoal.setForceExplode(true);
		creeper.goalSelector.addGoal(2, swellGoal);

		if (launch) {
			creeper.goalSelector.addGoal(1, new EACreeperLaunchGoal(creeper));
			if (launch$explosionRadius > 0)
				compoundNBT.putByte("ExplosionRadius", launch$explosionRadius.byteValue());
		}
		creeper.readAdditionalSaveData(compoundNBT);
		MessageCreeperDataSync.syncCreeperToPlayers(creeper);
	}

	@SubscribeEvent
	public void livingDamageEvent(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !tntLike
				|| !event.getSource().is(DamageTypeTags.IS_EXPLOSION)
				|| !(event.getEntity() instanceof Creeper creeper))
			return;

		creeper.ignite();
	}

	@SubscribeEvent
	public void onCreeperRemoved(EntityLeaveLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Creeper creeper)
				|| !creeper.isDeadOrDying()
				|| creeper.level().isClientSide)
			return;

		if (blowUpOnDeath == BlowUpOnDeath.ALL || (blowUpOnDeath == BlowUpOnDeath.CHARGED && creeper.isPowered()) || (creeper.getPersistentData().getBoolean(ANGRY) && angry$explodeOnDeath)) {
			float f = creeper.isPowered() ? 2.0F : 1.0F;
			creeper.level().explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(), (float)creeper.explosionRadius * f, Level.ExplosionInteraction.MOB);
			creeper.spawnLingeringCloud();
		}
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
				|| !launch$particles)
			return;
		ServerLevel serverLevel = (ServerLevel) creeper.level();
		if (creeper.getPersistentData().getBoolean(LAUNCH)) {
			for(int j = 0; j < serverLevel.players().size(); ++j) {
				ServerPlayer serverplayer = serverLevel.players().get(j);
				serverLevel.sendParticles(serverplayer, ParticleTypes.CLOUD, true, creeper.getX(), creeper.getY() + 0.25d, creeper.getZ(), 8, 0.05, 0.05, 0.05, 0.025);
			}
		}
	}

	public void onCenaCreeperTick(Creeper creeper) {
		if (creeper.tickCount % 40 != 5
				|| !angry$particles)
			return;
		ServerLevel serverLevel = (ServerLevel) creeper.level();
		if (creeper.getPersistentData().getBoolean(ANGRY)) {
			for (int j = 0; j < serverLevel.players().size(); ++j) {
				ServerPlayer serverplayer = serverLevel.players().get(j);
				BlockPos blockpos = serverplayer.blockPosition();
				if (!blockpos.closerToCenterThan(new Vec3(creeper.getX(), creeper.getY() + 0.5d, creeper.getZ()), 16))
					continue;
				serverLevel.sendParticles(serverplayer, ParticleTypes.ANGRY_VILLAGER, false, creeper.getX(), creeper.getY() + 1.1d, creeper.getZ(), 1, 0.15, 0.15, 0.15, 0);
			}
		}
	}

	public static boolean shouldDisableFallingSwelling() {
		return Feature.isEnabled(CreeperSwell.class) && disableFallingSwelling;
	}

	public enum AngryCreeperSounds {
		NONE(null, null),
		CENA(EASounds.CREEPER_CENA_FUSE, EASounds.CREEPER_CENA_EXPLODE),
		WTF_BOOM(EASounds.WTF_BOOM_FUSE, EASounds.WTF_BOOM_EXPLODE),
		OLD_EXPLOSION(() -> SoundEvents.CREEPER_PRIMED, EASounds.OLD_EXPLODE);

		@Nullable
		public final Supplier<SoundEvent> fuse;
		@Nullable
		public final Supplier<SoundEvent> explode;

		AngryCreeperSounds(@Nullable Supplier<SoundEvent> fuse, @Nullable Supplier<SoundEvent> explode) {
			this.fuse = fuse;
			this.explode = explode;
		}
	}

	public enum BlowUpOnDeath {
		NONE,
		CHARGED,
		ALL
	}
}
