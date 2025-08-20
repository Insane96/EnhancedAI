package insane96mcp.enhancedai.modules.drowned;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.DROWNED, description = "Makes drowned immune to sunlight for a couple of seconds before starting to burn. Only entity types in the enhancedai:drowned/sun_resistant tag are affected by this feature.")
public class SunResistantDrowned extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("drowned/sun_resistant"));

	@Config(min = 0, description = "If > 0, mobs will be fire resistant for a while until the water in their body evaporates. During this time, they will not seek water")
	public static Integer sunResistant = 600;

	public static ResourceLocation TIME;
	public static EAIData<Integer> TICKS;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		TIME = this.createDataKey("time");
		TICKS = EAIData.ofInt(this.createDataKey("ticks"));
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof net.minecraft.world.entity.monster.Drowned drowned)
				|| !drowned.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		if (sunResistant > 0)
			TICKS.applyIfAbsent(drowned, sunResistant);
	}

	@SubscribeEvent
	public void onTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof net.minecraft.world.entity.monster.Drowned drowned)
				|| drowned.level().isClientSide)
			return;

		int sunResistantTicks = TICKS.get(drowned);
		if (sunResistantTicks <= 0)
			return;

		int sunResistantTime = ModNBTData.get(drowned, TIME, Integer.class);
		if (drowned.isInWater() && sunResistantTime > 0) {
			sunResistantTime -= 4;
		}
		else if (sunResistantTime <= sunResistantTicks && !drowned.isInFluidType() && drowned.level().isDay() && drowned.level().canSeeSky(drowned.blockPosition())) {
			if (++sunResistantTime < sunResistantTicks)
				drowned.clearFire();
			else if (sunResistantTime == sunResistantTicks)
				drowned.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE);
		}
		ModNBTData.put(drowned, TIME, sunResistantTime);
	}
}
