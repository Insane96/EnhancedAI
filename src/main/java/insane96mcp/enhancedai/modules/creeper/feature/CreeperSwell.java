package insane96mcp.enhancedai.modules.creeper.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.creeper.ai.AICreeperLaunchGoal;
import insane96mcp.enhancedai.modules.creeper.ai.AICreeperSwellGoal;
import insane96mcp.enhancedai.setup.EASounds;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.network.MessageCreeperDataSync;
import insane96mcp.insanelib.setup.ILStrings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Creeper Swell", description = "Various changes to Creepers exploding. Ignoring Walls, Walking Fuse and smarter exploding based off explosion size")
@LoadFeature(module = Modules.Ids.CREEPER)
public class CreeperSwell extends Feature {

	@Config(min = 0d, max = 1d)
	@Label(name = "Walking Fuse Chance", description = "Percentage chance for a Creeper to keep walking while exploding.")
	public static Double walkingFuseChance = 0.1d;
	@Config
	@Label(name = "Walking Fuse Speed Modifier", description = "Speed modifier when a walking fuse creeper is swelling.")
	public static Double walkingFuseSpeedModifier = -0.5d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Ignore Walls Chance", description = "Percentage chance for a Creeper to ignore walls while targeting a player. This means that a creeper will be able to explode if it's in the correct range from a player even if there's a wall between.")
	public static Double ignoreWallsChance = 0.1d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Launch Chance", description = "Launching creepers will try ignite and throw themselves at the player.")
	public static Double launchChance = 0.05d;
	@Config
	@Label(name = "Launch Particles", description = "If true, Launching Creepers emit particles")
	public static Boolean launchParticles = true;

	@Config
	@Label(name = "Launch inaccuracy", description = "The inaccuracy of the launching creeper in Normal difficulty, easy is increased, hard is decreased.")
	public static Double launchInaccuracy = 0.5d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Breach Chance", description = "Breaching creepers will try to open an hole in the wall to let mobs in.")
	public static Double breachChance = 0.075d;
	@Config
	@Label(name = "TNT Like", description = "If true creepers will ignite if damaged by an explosion.")
	public static Boolean tntLike = false;
	//Cena
	@Config(min = 0d, max = 1d)
	@Label(name = "Cena.Chance", description = "AND HIS NAME IS ...")
	public static Double cenaChance = 0.02d;
	@Config
	@Label(name = "Cena.Particles", description = "If true, Creeper Cena emits particles")
	public static Boolean cenaParticles = true;
	@Config
	@Label(name = "Cena.Generates fire", description = "If true, Creeper Cena explosion will generate fire")
	public static Boolean cenaFire = false;
	@Config(min = 0d, max = 12d)
	@Label(name = "Cena.Explosion power", description = "Explosion power of Creeper Cena")
	public static Double cenaExplosionPower = 4d;

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

		if (creeper.getPersistentData().getBoolean(EAStrings.Tags.Creeper.CENA))
			creeper.playSound(EASounds.CREEPER_CENA_EXPLODE.get(), 4.0f, 1.0f);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void eventEntityJoinWorld(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Creeper creeper)) return;

		//Remove Creeper Swell Goal
		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		creeper.goalSelector.availableGoals.forEach(prioritizedGoal -> {
			if (prioritizedGoal.getGoal() instanceof SwellGoal)
				goalsToRemove.add(prioritizedGoal.getGoal());
		});

		goalsToRemove.forEach(creeper.goalSelector::removeGoal);

		CompoundTag persistentData = creeper.getPersistentData();

		boolean walkingFuse = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.WALKING_FUSE, creeper.getRandom().nextDouble() < walkingFuseChance);
		boolean ignoreWalls = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.IGNORE_WALLS, creeper.getRandom().nextDouble() < ignoreWallsChance);
		boolean breach = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.BREACH, creeper.getRandom().nextDouble() < breachChance);
		boolean launch = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.LAUNCH, creeper.getRandom().nextDouble() < launchChance);
		boolean cena = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.CENA, creeper.getRandom().nextDouble() < cenaChance);

		if (cena) {
			creeper.setCustomName(Component.literal("Creeper Cena"));
			CompoundTag compoundNBT = new CompoundTag();
			creeper.addAdditionalSaveData(compoundNBT);
			compoundNBT.putShort("Fuse", (short)36);
			compoundNBT.putByte("ExplosionRadius", cenaExplosionPower.byteValue());
			creeper.readAdditionalSaveData(compoundNBT);
			MessageCreeperDataSync.syncCreeperToPlayers(creeper);
			if (cenaFire)
				persistentData.putBoolean(ILStrings.Tags.EXPLOSION_CAUSES_FIRE, true);
		}

		AICreeperSwellGoal swellGoal = new AICreeperSwellGoal(creeper)
				.setWalkingFuse(walkingFuse)
				.setIgnoreWalls(ignoreWalls)
				.setBreaching(breach);
		creeper.goalSelector.addGoal(2, swellGoal);

		if (launch)
			creeper.goalSelector.addGoal(1, new AICreeperLaunchGoal(creeper));
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
	public void onCreeperTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Creeper creeper)
				|| creeper.level.isClientSide)
			return;

		onLaunchCreeperTick(creeper);
		onCenaCreeperTick(creeper);
	}

	public void onLaunchCreeperTick(Creeper creeper) {
		if (creeper.tickCount % 20 != 0
				|| !launchParticles)
			return;
		ServerLevel serverLevel = (ServerLevel) creeper.level;
		if (creeper.getPersistentData().getBoolean(EAStrings.Tags.Creeper.LAUNCH)) {
			for(int j = 0; j < serverLevel.players().size(); ++j) {
				ServerPlayer serverplayer = serverLevel.players().get(j);
				serverLevel.sendParticles(serverplayer, ParticleTypes.CLOUD, true, creeper.getX(), creeper.getY() + 0.5d, creeper.getZ(), 10, 0.1, 0.1, 0.1, 0.1);
			}
		}
	}

	public void onCenaCreeperTick(Creeper creeper) {
		if (creeper.tickCount % 40 != 5
				|| !cenaParticles)
			return;
		ServerLevel serverLevel = (ServerLevel) creeper.level;
		if (creeper.getPersistentData().getBoolean(EAStrings.Tags.Creeper.CENA)) {
			for(int j = 0; j < serverLevel.players().size(); ++j) {
				ServerPlayer serverplayer = serverLevel.players().get(j);
				BlockPos blockpos = serverplayer.blockPosition();
				if (!blockpos.closerToCenterThan(new Vec3(creeper.getX(), creeper.getY() + 0.5d, creeper.getZ()), 16))
					continue;
				serverLevel.sendParticles(serverplayer, ParticleTypes.ANGRY_VILLAGER, false, creeper.getX(), creeper.getY() + 1.1d, creeper.getZ(), 1, 0.15, 0.15, 0.15, 0);
			}
		}
	}

	/*public static boolean onCreeperScale(Creeper creeper, PoseStack poseStack, float partialTicks) {
		if (!Feature.isEnabled(CreeperSwell.class))
			return false;

		float swelling = creeper.getSwelling(partialTicks);
		float explosionPower = CreeperUtils.getExplosionSize(creeper);
		float f1 = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
		swelling = Mth.clamp(swelling, 0.0F, 1.0F);
		swelling *= swelling;
		swelling *= swelling;
		swelling *= 5;
		float f2 = (1.0F + swelling * 0.4F) / f1;
		float f3 = (1.0F + swelling * 0.2F) / f1;
		poseStack.scale(f2, f3, f2);
		return true;
	}*/
}
