package insane96mcp.enhancedai.modules.creeper.feature;

import insane96mcp.enhancedai.modules.creeper.ai.AICreeperLaunchGoal;
import insane96mcp.enhancedai.modules.creeper.ai.AICreeperSwellGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.ModSounds;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.entity.ai.goal.CreeperSwellGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Creeper Swell", description = "Various changes to Creepers exploding. Ignoring Walls, Walking Fuse and smarter exploding based off explosion size")
public class CreeperSwellFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> cenaChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> walkingFuseConfig;
	private final ForgeConfigSpec.ConfigValue<Double> ignoreWallsConfig;
	private final ForgeConfigSpec.ConfigValue<Double> breachConfig;
	private final ForgeConfigSpec.ConfigValue<Double> launchConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> tntLikeConfig;

	public double cenaChance = 0.02d;
	public double walkingFuseChance = 0.1d;
	public double ignoreWalls = 0.1d;
	public double breach = 0.05;
	public double launch = 0.05;
	public boolean tntLike = false;

	public CreeperSwellFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		cenaChanceConfig = Config.builder
				.comment("AND HIS NAME IS ...")
				.defineInRange("Cena Chance", cenaChance, 0d, 1d);
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
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		cenaChance = cenaChanceConfig.get();
		walkingFuseChance = walkingFuseConfig.get();
		ignoreWalls = ignoreWallsConfig.get();
		breach = breachConfig.get();
		launch = this.launchConfig.get();
		tntLike = tntLikeConfig.get();
	}

	@SubscribeEvent
	public void explosionStartEvent(ExplosionEvent.Detonate event) {
		if (!this.isEnabled())
			return;

		Explosion e = event.getExplosion();

		if (!(e.getExploder() instanceof CreeperEntity))
			return;

		CreeperEntity creeper = (CreeperEntity) e.getExploder();

		if (creeper.getPersistentData().getBoolean(Strings.Tags.JOHN_CENA))
			creeper.playSound(ModSounds.CREEPER_CENA_EXPLODE.get(), 4.0f, 1.0f);
	}

	@SubscribeEvent
	public void onExplosionStart(ExplosionEvent.Start event) {
		if (!this.isEnabled())
			return;

		Explosion explosion = event.getExplosion();
		if (!(explosion.getExploder() instanceof CreeperEntity))
			return;

		CreeperEntity creeper = (CreeperEntity) explosion.getExploder();

		CompoundNBT compoundNBT = creeper.getPersistentData();
		if (compoundNBT.getBoolean(Strings.Tags.EXPLOSION_CAUSES_FIRE))
			explosion.causesFire = true;
	}

	@SubscribeEvent
	public void eventEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof CreeperEntity))
			return;

		CreeperEntity creeper = (CreeperEntity) event.getEntity();

		ArrayList<Goal> goalsToRemove = new ArrayList<>();
		creeper.goalSelector.goals.forEach(prioritizedGoal -> {
			if (prioritizedGoal.getGoal() instanceof CreeperSwellGoal)
				goalsToRemove.add(prioritizedGoal.getGoal());
		});

		goalsToRemove.forEach(creeper.goalSelector::removeGoal);

		AICreeperSwellGoal swellGoal = new AICreeperSwellGoal(creeper, creeper.world.rand.nextDouble() < this.walkingFuseChance);
		if (creeper.world.rand.nextDouble() < this.cenaChance) {
			creeper.setCustomName(new StringTextComponent("Creeper Cena"));
			CompoundNBT compoundNBT = new CompoundNBT();
			compoundNBT.putShort("Fuse", (short)34);
			compoundNBT.putByte("ExplosionRadius", (byte)6);
			compoundNBT.putBoolean("powered", creeper.isCharged());
			creeper.readAdditional(compoundNBT);
			creeper.getPersistentData().putBoolean(Strings.Tags.EXPLOSION_CAUSES_FIRE, true);
			creeper.getPersistentData().putBoolean(Strings.Tags.JOHN_CENA, true);
		}
		swellGoal.setIgnoreWalls(creeper.world.rand.nextDouble() < this.ignoreWalls);
		swellGoal.setBreaching(creeper.world.rand.nextDouble() < this.breach);
		creeper.goalSelector.addGoal(2, swellGoal);

		if (creeper.world.rand.nextDouble() < this.launch) {
			creeper.goalSelector.addGoal(1, new AICreeperLaunchGoal(creeper));
			creeper.getPersistentData().putBoolean(Strings.Tags.LAUNCH, true);
		}
	}

	@SubscribeEvent
	public void livingDamageEvent(LivingDamageEvent event) {
		if (!this.tntLike)
			return;

		if (!event.getSource().isExplosion())
			return;

		if (!(event.getEntityLiving() instanceof CreeperEntity))
			return;

		CreeperEntity creeper = (CreeperEntity) event.getEntityLiving();
		creeper.ignite();
	}
}
