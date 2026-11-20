package insane96mcp.enhancedai.module.phantom;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@LoadFeature(module = EAIModules.PHANTOM, description = "Phantoms grab the entities they hit and carry them in the air until the grabbed entity hits them back. Only phantoms in the enhancedai:phantom/grab_targets entity type tag are affected by this feature.")
public class GrabbingPhantoms extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.id("phantom/grab_targets"));

	@Config(min = 0d, max = 1d, description = "Chance for a phantom to be able to grab targets. Rolled when the phantom spawns.")
	public static Double chance = 1d;
	@Config(min = 0, max = 6000, description = "Ticks of Slow Falling given to the entities hit and released by the phantom. Set to 0 to disable.")
	public static Integer slowFallingDuration = 200;

	public static EAIData<Boolean> GRAB_TARGETS;
	public static EAIData<Integer> SLOW_FALLING_DURATION;

	/// Set while a phantom is releasing its passenger, to let the dismount through.
	private static boolean releasing = false;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		GRAB_TARGETS = EAIData.ofBool(this.createDataKey("grab_targets"));
		SLOW_FALLING_DURATION = EAIData.ofInt(this.createDataKey("slow_falling_duration"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Phantom phantom)
				|| !phantom.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		SLOW_FALLING_DURATION.applyIfAbsent(phantom, slowFallingDuration);
		GRAB_TARGETS.applyIfAbsent(phantom, phantom.getRandom().nextDouble() < chance);
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent.Post event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide)
			return;

		LivingEntity victim = event.getEntity();
		// The phantom hits the target: slow falling and grab
		if (event.getSource().getDirectEntity() instanceof Phantom phantom
				&& phantom.getType().is(AFFECTED_ENTITY_TYPES)) {
			applySlowFalling(phantom, victim);
			if (GRAB_TARGETS.get(phantom))
				tryGrab(phantom, victim);
		}
		// The grabbed entity hits the phantom (also with projectiles): release
		else if (victim instanceof Phantom phantom
				&& isGrabbing(phantom)
				&& event.getSource().getEntity() != null
				&& phantom.hasPassenger(event.getSource().getEntity())) {
			release(phantom);
		}
	}

	private static void tryGrab(Phantom phantom, LivingEntity victim) {
		if (!phantom.isAlive()
				|| !victim.isAlive()
				|| phantom.isVehicle()
				|| phantom.isPassenger()
				|| victim.isVehicle()
				|| (victim instanceof Player player && (player.isCreative() || player.isSpectator())))
			return;

		if (victim.isPassenger())
			victim.stopRiding();
		// Dismount might have been prevented (e.g. already grabbed by another phantom)
		if (victim.isPassenger())
			return;
		victim.startRiding(phantom, true);
	}

	public static void release(Phantom phantom) {
		releasing = true;
		try {
			phantom.ejectPassengers();
		}
		finally {
			releasing = false;
		}
	}

	public static boolean isGrabbing(Phantom phantom) {
		return GRAB_TARGETS.get(phantom) && phantom.isVehicle();
	}

	private static void applySlowFalling(Phantom phantom, LivingEntity entity) {
		if (SLOW_FALLING_DURATION.get(phantom) > 0)
			entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SLOW_FALLING_DURATION.get(phantom)), phantom);
	}

	/// No damage to the grabbed entity while being carried
	@SubscribeEvent
	public void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| !(event.getSource().getDirectEntity() instanceof Phantom phantom)
				|| !phantom.getType().is(AFFECTED_ENTITY_TYPES)
				|| !isGrabbing(phantom)
				|| !phantom.hasPassenger(event.getEntity()))
			return;

		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onDismount(EntityMountEvent event) {
		if (!this.isEnabled()
				|| !event.isDismounting()
				|| event.getLevel().isClientSide
				|| !(event.getEntityBeingMounted() instanceof Phantom phantom)
				|| !phantom.getType().is(AFFECTED_ENTITY_TYPES)
				|| !GRAB_TARGETS.get(phantom)
				|| !(event.getEntityMounting() instanceof LivingEntity grabbed))
			return;

		boolean canDismount = releasing
				|| !phantom.isAlive()
				|| phantom.isDeadOrDying()
				|| !grabbed.isAlive()
				|| (grabbed instanceof Player player && (player.isCreative() || player.isSpectator()));
		if (!canDismount) {
			event.setCanceled(true);
			return;
		}

		// Slow falling again on release, as the grabbed entity might have been carried for long
		if (grabbed.isAlive())
			applySlowFalling(phantom, grabbed);
	}

	/// True if the phantom's passengers should hang below it. Used client side too, so it doesn't rely on data attachments.
	public static boolean shouldHangBelow(Phantom phantom) {
		return Feature.isEnabled(GrabbingPhantoms.class)
				&& phantom.getType().is(AFFECTED_ENTITY_TYPES);
	}
}
