package insane96mcp.enhancedai.modules.spider.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.spider.ai.WebThrowGoal;
import insane96mcp.enhancedai.modules.spider.entity.projectile.TemporaryCobwebTask;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.scheduled.ScheduledTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
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
	public static Double thrownWebDamage = 3d;
	@Config(min = 1, max = 1200)
	@Label(name = "Cooldown", description = "Every how many ticks do spiders throw the projectile")
	public static MinMax throwingCooldown = new MinMax(40, 60);
	@Config(min = 0d, max = 64d)
	@Label(name = "Distance Required", description = "Distance Required for the spider to throw webs. Setting 'Minimum' to 0 will make the spider throw webs even when attacking the player.")
	public static MinMax distance = new MinMax(2.5d, 32d);
	@Config
	@Label(name = "Always web", description = "If true entities will get webbed when hit.")
	public static Boolean alwaysWeb = false;
	@Config
	@Label(name = "Cave spiders poisonous webs", description = "If true cave spiders' thrown web will poison entities hit like when they hit the entity melee.")
	public static Boolean caveSpidersPoisonousWebs = true;
	@Config
	@Label(name = "Apply Speed on hit", description = "If true, spiders will gain a speed boost when they hit the target.")
	public static Boolean applySlowness = true;
	@Config
	@Label(name = "Apply Slowness", description = "If true entities will get slowness when hit.")
	public static Boolean applySpeed = true;
	//Slowness
	@Config(min = 0d, max = 6000)
	@Label(name = "Slowness.Duration", description = "How many ticks of slowness are applied to the target hit by the web?")
	public static Integer slownessDuration = 120;
	@Config(min = 0, max = 128)
	@Label(name = "Slowness.Amplifier", description = "How many levels of slowness are applied to the target hit by the web?")
	public static Integer slownessAmplifier = 0;
	@Config
	@Label(name = "Slowness.Stacking Amplifier", description = "Should multiple hits on a target with slowness increase the level of Slowness? (This works with any type of slowness)")
	public static Boolean stackSlowness = false;
	@Config(min = 0, max = 128)
	@Label(name = "Slowness.Max Amplifier", description = "How many max levels of slowness can be applied to the target if Staking amplifier is enabled?")
	public static Integer maxSlowness = 2;
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

		boolean webThrower = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Spider.WEB_THROWER, spider.getRandom().nextDouble() < webThrowChance);

		if (webThrower)
			spider.goalSelector.addGoal(2, new WebThrowGoal(spider));
	}

	public static void applyEffects(LivingEntity spider, LivingEntity entity) {
		applySlowness(entity);
		applyPoison(spider, entity);
	}

	public static void applySlowness(LivingEntity entity) {
		if (!applySlowness)
			return;
		MobEffectInstance slowness = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

		if (stackSlowness && slowness != null)
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, Math.min(slowness.getAmplifier() + slownessAmplifier + 1, maxSlowness - 1), true, true, true));
		else
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, slownessAmplifier, true, true, true));
	}

	public static void applyPoison(LivingEntity spider, LivingEntity entity) {
		if (!caveSpidersPoisonousWebs
				|| !(spider instanceof CaveSpider caveSpider))
			return;
		int i = 0;
		if (caveSpider.level.getDifficulty() == Difficulty.NORMAL) {
			i = 7;
		} else if (caveSpider.level.getDifficulty() == Difficulty.HARD) {
			i = 15;
		}

		if (i > 0) {
			entity.addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), caveSpider);
		}
	}

	public static void applyWeb(LivingEntity entity) {
		if (!alwaysWeb)
			return;
		BlockPos spawnCobwebAt = entity.blockPosition();
		if (FallingBlock.isFree(entity.level.getBlockState(spawnCobwebAt))) {
			entity.level.setBlock(spawnCobwebAt, Blocks.COBWEB.defaultBlockState(), 3);
			ScheduledTasks.schedule(new TemporaryCobwebTask(ThrowingWeb.destroyWebAfter, entity.level, spawnCobwebAt));
			for(int i = 0; i < 32; ++i) {
				entity.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), spawnCobwebAt.getX() + entity.getRandom().nextDouble(), spawnCobwebAt.getY() + entity.getRandom().nextDouble(), spawnCobwebAt.getZ() + entity.getRandom().nextDouble(), 0d, 0D, 0d);
			}
			entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
		}
	}
}