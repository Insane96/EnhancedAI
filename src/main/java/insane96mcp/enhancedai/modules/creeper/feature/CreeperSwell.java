package insane96mcp.enhancedai.modules.creeper.feature;

import insane96mcp.enhancedai.modules.creeper.ai.AICreeperLaunchGoal;
import insane96mcp.enhancedai.modules.creeper.ai.AICreeperSwellGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EASounds;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.setup.ILStrings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Creeper Swell", description = "Various changes to Creepers exploding. Ignoring Walls, Walking Fuse and smarter exploding based off explosion size")
public class CreeperSwell extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> walkingFuseConfig;
	private final ForgeConfigSpec.ConfigValue<Double> ignoreWallsConfig;
	private final ForgeConfigSpec.ConfigValue<Double> breachConfig;
	private final ForgeConfigSpec.ConfigValue<Double> launchConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> tntLikeConfig;
	//Cena
	private final ForgeConfigSpec.DoubleValue cenaChanceConfig;
	private final ForgeConfigSpec.BooleanValue cenaFireConfig;
	private final ForgeConfigSpec.DoubleValue cenaExplosionPowerConfig;

	public double walkingFuseChance = 0.1d;
	public double ignoreWalls = 0.1d;
	public double breach = 0.075d;
	public double launch = 0.05d;
	public boolean tntLike = false;
	//Cena
	public double cenaChance = 0.02d;
	public boolean cenaFire = false;
	public double cenaExplosionPower = 6;

	public CreeperSwell(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		walkingFuseConfig = Config.builder
				.comment("Percentage chance for a Creeper to not stand still while exploding.")
				.defineInRange("Walking Fuse Chance", walkingFuseChance, 0d, 1d);
		ignoreWallsConfig = Config.builder
				.comment("Percentage chance for a Creeper to ignore walls while targeting a player. This means that a creeper will be able to explode if it's in the correct range from a player even if there's a wall between.")
				.defineInRange("Ignore Walls Chance", ignoreWalls, 0d, 1d);
		breachConfig = Config.builder
				.comment("Breaching creepers will try to open an hole in the wall to let mobs in.")
				.defineInRange("Breach Chance", breach, 0d, 1d);
		launchConfig = Config.builder
				.comment("Launching creepers will try ignite and throw themselves at the player.")
				.defineInRange("Launch Chance", this.launch, 0d, 1d);
		tntLikeConfig = Config.builder
				.comment("If true creepers will ignite if damaged by an explosion.")
				.define("TNT Like", tntLike);
		Config.builder.push("Creeper Cena");
		cenaChanceConfig = Config.builder
				.comment("AND HIS NAME IS ...")
				.defineInRange("Cena Chance", cenaChance, 0d, 1d);
		cenaFireConfig = Config.builder
				.comment("If true, Creeper Cena explosion will generate fire")
				.define("Cena generates fire", this.cenaFire);
		cenaExplosionPowerConfig = Config.builder
				.comment("Explosion power of Creeper Cena")
				.defineInRange("Cena explosion power", this.cenaExplosionPower, 0d, 12d);
		Config.builder.pop();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.walkingFuseChance = walkingFuseConfig.get();
		this.ignoreWalls = ignoreWallsConfig.get();
		this.breach = breachConfig.get();
		this.launch = this.launchConfig.get();
		this.tntLike = tntLikeConfig.get();
		//Cena
		this.cenaChance = this.cenaChanceConfig.get();
		this.cenaFire = this.cenaFireConfig.get();
		this.cenaExplosionPower = this.cenaExplosionPowerConfig.get();
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
	public void eventEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Creeper creeper))
			return;

		//Remove Creeper Swell Goal
		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		creeper.goalSelector.availableGoals.forEach(prioritizedGoal -> {
			if (prioritizedGoal.getGoal() instanceof SwellGoal)
				goalsToRemove.add(prioritizedGoal.getGoal());
		});

		goalsToRemove.forEach(creeper.goalSelector::removeGoal);

		CompoundTag persistentData = creeper.getPersistentData();

		boolean walkingFuse = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.WALKING_FUSE, creeper.level.random.nextDouble() < this.walkingFuseChance);
		boolean ignoreWalls = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.IGNORE_WALLS, creeper.level.random.nextDouble() < this.ignoreWalls);
		boolean breach = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.BREACH, creeper.level.random.nextDouble() < this.breach);
		boolean launch = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.LAUNCH, creeper.level.random.nextDouble() < this.launch);
		boolean cena = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Creeper.CENA, creeper.level.random.nextDouble() < this.cenaChance);

		if (cena) {
			creeper.setCustomName(new TextComponent("Creeper Cena"));
			CompoundTag compoundNBT = new CompoundTag();
			creeper.addAdditionalSaveData(compoundNBT);
			compoundNBT.putShort("Fuse", (short)36);
			compoundNBT.putByte("ExplosionRadius", (byte)this.cenaExplosionPower);
			creeper.readAdditionalSaveData(compoundNBT);
			if (this.cenaFire)
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
				|| !event.getSource().isExplosion()
				|| !(event.getEntityLiving() instanceof Creeper creeper)
				|| !this.tntLike)
			return;

		creeper.ignite();
	}
}
