package insane96mcp.enhancedai.modules.spider.feature;

import insane96mcp.enhancedai.modules.spider.ai.WebThrowGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Throwing Web", description = "Makes spiders throw a web at a player, slowing them.")
public class ThrowingWeb extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> webThrowChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> destroyWebAfterConfig;
	private final ForgeConfigSpec.ConfigValue<Double> thrownWebDamageConfig;
	private final MinMax.Config throwingCooldownConfig;
	private final MinMax.Config distanceConfig;
	//Slowness
	private final ForgeConfigSpec.ConfigValue<Integer> slownessTimeConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> slownessAmplifierConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> stackSlownessConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxSlownessConfig;

	private final Blacklist.Config entityBlacklistConfig;

	public double webThrowChance = 0.1d;
	public int destroyWebAfter = 100;
	public double thrownWebDamage = 5d;
	public MinMax throwingCooldown = new MinMax(40, 60);
	public MinMax distance = new MinMax(2.5d, 64d);
	//Slowness
	public int slownessTime = 120;
	public int slownessAmplifier = 2;
	public boolean stackSlowness = true;
	public int maxSlowness = 6;

	public Blacklist entityBlacklist;

	public ThrowingWeb(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		webThrowChanceConfig = Config.builder
				.comment("Chance for a Spider to spawn with the ability to throw webs at the target.")
				.defineInRange("Web Throw Chance", this.webThrowChance, 0d, 1d);
		destroyWebAfterConfig = Config.builder
				.comment("After how many ticks will the web projectile's cobweb be destroyed?")
				.defineInRange("Destroy Web After", this.destroyWebAfter, 0, 6000);
		thrownWebDamageConfig = Config.builder
				.comment("Damage when the projectiles hits a mob. The damage is set for normal difficulty. Hard difficulty gets +50% damage and Easy gets (-50% + 1) damage.")
				.defineInRange("Web Damage", this.thrownWebDamage, 0d, 128d);
		this.throwingCooldownConfig = new MinMax.Config(Config.builder, "Projectile cooldown", "Every how many ticks do spiders throw the projectile")
				.setMinMax(1, 1200, this.throwingCooldown)
				.build();
		this.distanceConfig = new MinMax.Config(Config.builder, "Distance Required", "Distance Required for the spider to throw webs. Setting 'Minimum' to 0 will make the spider throw webs even when attacking the player.")
				.setMinMax(0d, 64d, this.distance)
				.build();

		Config.builder.push("Slowness");
		slownessTimeConfig = Config.builder
				.comment("How many ticks of slowness are applied to the target hit by the web?")
				.defineInRange("Slowness Tick", this.slownessTime, 0, 6000);
		slownessAmplifierConfig = Config.builder
				.comment("How many levels of slowness are applied to the target hit by the web?")
				.defineInRange("Slowness Amplifier", this.slownessAmplifier, 0, 128);
		stackSlownessConfig = Config.builder
				.comment("Should multiple hits on a target with slowness increase the level of Slowness? (This works with any type of slowness)")
				.define("Stack Slowness Amplifier", this.stackSlowness);
		maxSlownessConfig = Config.builder
				.comment("How many max levels of slowness can be applied to the target?")
				.defineInRange("Max Slowness Amplifier", this.maxSlowness, 0, 128);
		Config.builder.pop();

		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Throwing Web AI")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.webThrowChance = this.webThrowChanceConfig.get();
		this.destroyWebAfter = this.destroyWebAfterConfig.get();
		this.thrownWebDamage = this.thrownWebDamageConfig.get();
		this.throwingCooldown = this.throwingCooldownConfig.get();
		this.distance = this.distanceConfig.get();
		//Slowness
		this.slownessTime = this.slownessTimeConfig.get();
		this.slownessAmplifier = this.slownessAmplifierConfig.get();
		this.stackSlowness = this.stackSlownessConfig.get();
		this.maxSlowness = this.maxSlownessConfig.get();

		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Spider spider))
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(spider))
			return;

		CompoundTag persistentData = spider.getPersistentData();

		boolean webThrower = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Spider.WEB_THROWER, spider.level.random.nextDouble() < this.webThrowChance);

		if (webThrower)
			spider.goalSelector.addGoal(2, new WebThrowGoal(spider));
	}

	public void applySlowness(LivingEntity entity) {
		MobEffectInstance slowness = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

		if (slowness == null)
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, this.slownessTime, this.slownessAmplifier - 1, true, true, true));
		else if (this.stackSlowness)
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, this.slownessTime, Math.min(slowness.getAmplifier() + this.slownessAmplifier, this.maxSlowness - 1), true, true, true));
	}
}