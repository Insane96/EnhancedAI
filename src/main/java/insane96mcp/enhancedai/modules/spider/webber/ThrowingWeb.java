package insane96mcp.enhancedai.modules.spider.webber;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SPIDER, description = "Makes spiders throw a web at a player, slowing them")
public class ThrowingWeb extends Feature {
	public static final TagKey<EntityType<?>> CAN_THROW_WEBS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_throw_webs"));
	public static final String WEB_THROWER = EnhancedAI.RESOURCE_PREFIX + "web_thrower";
	@Config(min = 0d, max = 1d, description = "Chance for a Spider to spawn with the ability to throw webs at the target.")
	public static Double chance = 0.1d;
	@Config(min = 0, max = 6000, description = "After how many ticks will the cobweb placed by the web projectile be destroyed?")
	public static Integer destroyWebAfter = 100;
	@Config(min = 0, max = 128d, description = "Damage when the projectiles hits a mob. The damage is set for normal difficulty. Hard difficulty gets +50% damage and Easy gets (-50% + 1) damage.")
	public static Double damage = 3d;
	@Config(min = 1, max = 1200, description = "Every how many ticks do spiders throw the projectile")
	public static MinMax cooldown = new MinMax(40, 60);
	@Config(min = 0d, max = 64d, description = "Distance Required for the spider to throw webs. Setting 'Minimum' to 0 will make the spider throw webs even when attacking the player.")
	public static MinMax distance = new MinMax(2.5d, 32d);
	@Config(description = "If true entities will get webbed when hit.")
	public static Boolean alwaysWeb = false;
	@Config(description = "If true cave spiders' thrown web will poison entities hit like when they hit the entity melee.")
	public static Boolean caveSpidersPoisonousWebs = true;
	@Config(description = "If true, spiders will gain a speed boost when they hit the target.")
	public static Boolean applySpeed = true;
	@Config(description = "If true entities will get slowness when hit.")
	public static Boolean applySlowness = true;
	//Slowness
	@Config(min = 0d, max = 6000, description = "How many ticks of slowness are applied to the target hit by the web?")
	public static Integer slowness$duration = 120;
	@Config(min = 0, max = 128, description = "How many levels of slowness are applied to the target hit by the web?")
	public static Integer slowness$amplifier = 0;
	@Config(description = "Should multiple hits on a target with slowness increase the level of Slowness? (This works with any type of slowness)")
	public static Boolean slowness$stackAmplifier = false;
	@Config(min = 0, max = 128, description = "How many max levels of slowness can be applied to the target if Staking amplifier is enabled?")
	public static Integer maxSlowness = 2;

	public ThrowingWeb(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Spider spider)
				|| !spider.getType().is(CAN_THROW_WEBS))
			return;

		CompoundTag persistentData = spider.getPersistentData();

		boolean webThrower = NBTUtils.getBooleanOrPutDefault(persistentData, WEB_THROWER, spider.getRandom().nextDouble() < chance);

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

		if (slowness$stackAmplifier && slowness != null)
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowness$duration, Math.min(slowness.getAmplifier() + slowness$amplifier + 1, maxSlowness - 1), false, false, true));
		else
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowness$duration, slowness$amplifier, false, false, true));
	}

	public static void applyPoison(LivingEntity spider, LivingEntity entity) {
		if (!caveSpidersPoisonousWebs
				|| !(spider instanceof CaveSpider caveSpider))
			return;
		int i = 0;
		if (caveSpider.level().getDifficulty() == Difficulty.NORMAL) {
			i = 7;
		} else if (caveSpider.level().getDifficulty() == Difficulty.HARD) {
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
		if (FallingBlock.isFree(entity.level().getBlockState(spawnCobwebAt))) {
			entity.level().setBlock(spawnCobwebAt, Blocks.COBWEB.defaultBlockState(), 3);
			ScheduledTasks.schedule(new TemporaryCobwebTask(ThrowingWeb.destroyWebAfter, entity.level(), spawnCobwebAt));
			for(int i = 0; i < 32; ++i) {
				entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), spawnCobwebAt.getX() + entity.getRandom().nextDouble(), spawnCobwebAt.getY() + entity.getRandom().nextDouble(), spawnCobwebAt.getZ() + entity.getRandom().nextDouble(), 0d, 0D, 0d);
			}
			entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
		}
	}
}