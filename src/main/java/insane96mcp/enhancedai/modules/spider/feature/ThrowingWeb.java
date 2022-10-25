package insane96mcp.enhancedai.modules.spider.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.spider.ai.WebThrowGoal;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Throwing Web", description = "Makes spiders throw a web at a player, slowing them.")
@LoadFeature(module = Modules.Ids.SPIDER)
public class ThrowingWeb extends Feature {
	@Config(min = 0d, max = 1d)
	@Label(name = "Web Throw Chance", description = "Chance for a Spider to spawn with the ability to throw webs at the target.")
	public static Double webThrowChance = 0.1d;
	@Config(min = 0, max = 6000)
	@Label(name = "Destroy Web After", description = "After how many ticks will the cobweb placed by the web projectile be destroyed?")
	public static Integer destroyWebAfter = 100;
	@Config(min = 0, max = 128d)
	@Label(name = "Web Damage", description = "Damage when the projectiles hits a mob. The damage is set for normal difficulty. Hard difficulty gets +50% damage and Easy gets (-50% + 1) damage.")
	public static Double thrownWebDamage = 5d;
	@Config(min = 1, max = 1200)
	@Label(name = "Cooldown", description = "Every how many ticks do spiders throw the projectile")
	public static MinMax throwingCooldown = new MinMax(40, 60);
	@Config(min = 0d, max = 64d)
	@Label(name = "Distance Required", description = "Distance Required for the spider to throw webs. Setting 'Minimum' to 0 will make the spider throw webs even when attacking the player.")
	public static MinMax distance = new MinMax(2.5d, 64d);
	//Slowness
	@Config(min = 0d, max = 6000)
	@Label(name = "Slowness.Duration", description = "How many ticks of slowness are applied to the target hit by the web?")
	public static Integer slownessDuration = 120;
	@Config(min = 0d, max = 128)
	@Label(name = "Slowness.Amplifier", description = "How many levels of slowness are applied to the target hit by the web?")
	public static Integer slownessAmplifier = 2;
	@Config
	@Label(name = "Slowness.Stacking Amplifier", description = "Should multiple hits on a target with slowness increase the level of Slowness? (This works with any type of slowness)")
	public static Boolean stackSlowness = true;
	@Config(min = 0d, max = 128)
	@Label(name = "Slowness.Max Amplifier", description = "How many max levels of slowness can be applied to the target?")
	public static Integer maxSlowness = 6;
	@Config
	@Label(name = "Entity Blacklist", description = "Entities that will not be affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public ThrowingWeb(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Spider spider)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(spider))
			return;

		CompoundTag persistentData = spider.getPersistentData();

		boolean webThrower = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Spider.WEB_THROWER, spider.level.random.nextDouble() < webThrowChance);

		if (webThrower)
			spider.goalSelector.addGoal(2, new WebThrowGoal(spider));
	}

	public static void applySlowness(LivingEntity entity) {
		MobEffectInstance slowness = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

		if (slowness == null)
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, slownessAmplifier - 1, true, true, true));
		else if (stackSlowness)
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, Math.min(slowness.getAmplifier() + slownessAmplifier, maxSlowness - 1), true, true, true));
	}
}