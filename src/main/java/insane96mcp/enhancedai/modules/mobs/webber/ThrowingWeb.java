package insane96mcp.enhancedai.modules.mobs.webber;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes mobs throw a web at a player, slowing them. Only entity types in the `enhancedai:mobs/throwing_web/can_throw_webs` tag will be affected. Entity types in `enhancedai:mobs/throwing_web/poisonous_webs` will throw poisonous webs and apply poison the same way as when a cave spider attacks an entity.")
public class ThrowingWeb extends Feature {
	public static final TagKey<EntityType<?>> CAN_THROW_WEBS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/throwing_web/can_throw_webs"));
	public static final TagKey<EntityType<?>> POISONOUS_WEBS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/throwing_web/poisonous_webs"));

	@Config(min = 0d, max = 1d, description = "Chance for a mob to spawn with the ability to throw webs at the target. Only entity types in the `enhancedai:mobs/throwing_web/can_throw_webs` tag are affected.")
	public static Double chance = 0.1d;
	@Config
	public static Boolean placeWebOnBlockHit = false;
	@Config
	public static Boolean placeWebOnEntityHit = false;
	@Config(description = "If true, entity types in `enhancedai:mobs/throwing_web/poisonous_webs` tag thrown webs' will poison entities hit like when cave spiders hit entity in melee.")
	public static Boolean poisonousWebs = true;
	@Config(min = -1, max = 6000, description = "After how many ticks will the cobweb placed by the web projectile be destroyed? Setting to -1 will prevent the cobweb from being destroyed.")
	public static Integer destroyWebAfter = 100;
	@Config(min = 0, max = 128d, description = "Damage when the projectiles hits a mob. The damage is set for normal difficulty. Hard difficulty gets +50% damage and Easy gets (-50% + 1) damage.")
	public static Double damage = 3d;
	@Config(min = 1, max = 1200, description = "Every how many ticks do mobs throw the projectile")
	public static MinMax cooldown = new MinMax(40, 60);
	@Config(min = 0d, max = 64d, description = "Distance Required for the mobs to throw webs. Setting 'Minimum' to 0 will make the mob throw webs even when attacking the player.")
	public static MinMax distance = new MinMax(2.5d, 32d);
	@Config(description = "If true, throwers will gain a speed boost when they hit the target but 5 seconds will be added to to cooldown.")
	public static Boolean applySpeed = true;
	//Slowness
	@Config(description = "If true entities will get slowness when hit.")
	public static Boolean slowness$enable = true;
	@Config(min = 0d, max = 6000, description = "How many ticks of slowness are applied to the target hit by the web?")
	public static Integer slowness$duration = 120;
	@Config(min = 0, max = 128, description = "How many levels of slowness are applied to the target hit by the web?")
	public static Integer slowness$amplifier = 0;
	@Config(description = "Should multiple hits on a target with slowness increase the level of Slowness? (This works with any type of slowness)")
	public static Boolean slowness$stackAmplifier = false;
	@Config(min = 0, max = 128, description = "How many max levels of slowness can be applied to the target if Staking amplifier is enabled?")
	public static Integer slowness$maxSlownessAmplifier = 2;

	public static EAIData<Boolean> WEB_THROWER;
	public static EAIData<Boolean> POISONOUS_WEB;
	public static EAIData<Boolean> PLACE_WEB_ON_BLOCK_HIT;
	public static EAIData<Boolean> PLACE_WEB_ON_ENTITY_HIT;
	public static EAIData<Integer> DESTROY_WEB_AFTER;
	public static EAIData<Double> DAMAGE;
	public static EAIData<Integer> COOLDOWN;
	public static EAIData<Double> DISTANCE_MIN;
	public static EAIData<Double> DISTANCE_MAX;
	public static EAIData<Boolean> APPLY_SPEED;
	public static EAIData<Boolean> APPLY_SLOWNESS;
	public static EAIData<Integer> SLOWNESS_DURATION;
	public static EAIData<Integer> SLOWNESS_AMPLIFIER;
	public static EAIData<Boolean> SLOWNESS_STACK_AMPLIFIER;
	public static EAIData<Integer> SLOWNESS_MAX_SLOWNESS_AMPLIFIER;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		WEB_THROWER = EAIData.ofBool(this.createDataKey("web_thrower"), (mob, canThrowWebs) -> {
			GoalHelper.removeGoal(mob.goalSelector, WebThrowGoal.class);
			if (canThrowWebs)
				mob.goalSelector.addGoal(2, new WebThrowGoal(mob));
		});
		POISONOUS_WEB = EAIData.ofBool(this.createDataKey("poisonous_web"));
		PLACE_WEB_ON_BLOCK_HIT = EAIData.ofBool(this.createDataKey("place_web_on_block_hit"));
		PLACE_WEB_ON_ENTITY_HIT = EAIData.ofBool(this.createDataKey("place_web_on_entity_hit"));
		DESTROY_WEB_AFTER = EAIData.ofInt(this.createDataKey("destroy_web_after"));
		DAMAGE = EAIData.ofDouble(this.createDataKey("damage"));
		COOLDOWN = EAIData.ofInt(this.createDataKey("cooldown"));
		DISTANCE_MIN = EAIData.ofDouble(this.createDataKey("distance_min"));
		DISTANCE_MAX = EAIData.ofDouble(this.createDataKey("distance_max"));
		APPLY_SPEED = EAIData.ofBool(this.createDataKey("apply_speed"));
		APPLY_SLOWNESS = EAIData.ofBool(this.createDataKey("slowness/enable"));
		SLOWNESS_DURATION = EAIData.ofInt(this.createDataKey("slowness/duration"));
		SLOWNESS_AMPLIFIER = EAIData.ofInt(this.createDataKey("slowness/amplifier"));
		SLOWNESS_STACK_AMPLIFIER = EAIData.ofBool(this.createDataKey("slowness/stack_amplifier"));
		SLOWNESS_MAX_SLOWNESS_AMPLIFIER = EAIData.ofInt(this.createDataKey("slowness/max_slowness_amplifier"));
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Mob mob))
			return;

		if (mob.getType().is(CAN_THROW_WEBS)) {
			WEB_THROWER.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
			PLACE_WEB_ON_BLOCK_HIT.applyIfAbsent(mob, placeWebOnBlockHit);
			PLACE_WEB_ON_ENTITY_HIT.applyIfAbsent(mob, placeWebOnEntityHit);
			DESTROY_WEB_AFTER.applyIfAbsent(mob, destroyWebAfter);
			DAMAGE.applyIfAbsent(mob, damage);
			COOLDOWN.applyIfAbsent(mob, cooldown.getIntRandBetween(mob.getRandom()));
			DISTANCE_MIN.applyIfAbsent(mob, distance.min);
			DISTANCE_MAX.applyIfAbsent(mob, distance.max);
			APPLY_SPEED.applyIfAbsent(mob, applySpeed);
			APPLY_SLOWNESS.applyIfAbsent(mob, slowness$enable);
			SLOWNESS_DURATION.applyIfAbsent(mob, slowness$duration);
			SLOWNESS_AMPLIFIER.applyIfAbsent(mob, slowness$amplifier);
			SLOWNESS_STACK_AMPLIFIER.applyIfAbsent(mob, slowness$stackAmplifier);
			SLOWNESS_MAX_SLOWNESS_AMPLIFIER.applyIfAbsent(mob, slowness$maxSlownessAmplifier);
			if (mob.getType().is(POISONOUS_WEBS))
				POISONOUS_WEB.applyIfAbsent(mob, poisonousWebs);
		}
	}

	public static void applyEffects(Entity thrower, LivingEntity entityHit) {
		applySlowness(thrower, entityHit);
		applyPoison(thrower, entityHit);
	}

	public static void applySlowness(Entity thrower, LivingEntity entityHit) {
		if (!APPLY_SLOWNESS.get(thrower))
			return;
		MobEffectInstance slowness = entityHit.getEffect(MobEffects.MOVEMENT_SLOWDOWN);

		if (SLOWNESS_STACK_AMPLIFIER.get(thrower) && slowness != null)
			entityHit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION.get(thrower), Math.min(slowness.getAmplifier() + SLOWNESS_AMPLIFIER.get(thrower) + 1, SLOWNESS_MAX_SLOWNESS_AMPLIFIER.get(thrower) - 1), false, false, true));
		else
			entityHit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION.get(thrower), SLOWNESS_AMPLIFIER.get(thrower), false, false, true));
	}

	public static void applyPoison(Entity thrower, LivingEntity entityHit) {
		if (!POISONOUS_WEB.get(thrower))
			return;
		int i = 0;
		Difficulty difficulty = thrower.level().getDifficulty();
		if (difficulty == Difficulty.NORMAL)
			i = 7;
		else if (difficulty == Difficulty.HARD)
			i = 15;

		if (i > 0)
			entityHit.addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), thrower);
	}

	public static void applyWeb(Entity thrower, BlockPos pos) {
		if (!(thrower.level() instanceof ServerLevel level))
			return;
		if (!level.getBlockState(pos).canBeReplaced())
			return;
		level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()), pos.getX(), pos.getY(), pos.getZ(), 32, 1, 1, 1, 1d);
		level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.0f, 0.5f);
		int tickDelay = DESTROY_WEB_AFTER.get(thrower);
		if (tickDelay == -1)
			return;
		ScheduledTasks.schedule(new TemporaryCobwebTask(tickDelay, level, pos));
	}
}