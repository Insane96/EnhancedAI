package insane96mcp.enhancedai.modules.mobs.blindnessaffected;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Makes mobs follow range actually affected by blindness effect. Only entity types in `enhancedai:mobs/blindness_range_multiplier` tag will be affected by this.")
public class ActualBlindness extends Feature {
	public static final TagKey<EntityType<?>> BLINDNESS_RANGE_MULTIPLIER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/blindness_range_multiplier"));

	public static final ResourceLocation BLINDNESS_FOLLOW_RANGE_ID = EnhancedAI.location("blindness_follow_range");

	@Config(min = 0d, max = 1d, description = "Follow range is multiplied by this value if the mob has blindness.")
	public static Double blindnessRangeMultiplier = .15d;

	public static EAIData<Double> BLINDNESS_RANGE_MULTIPLIER_DATA;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		BLINDNESS_RANGE_MULTIPLIER_DATA = EAIData.ofDouble(this.createDataKey("blindness_range_multiplier"));
	}

	//High priority as should run before specific mobs
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob))
			return;

		processBlindnessRangeMultiplier(mob);
	}

	private void processBlindnessRangeMultiplier(Mob mob) {
		if (!mob.getType().is(BLINDNESS_RANGE_MULTIPLIER))
			return;
		BLINDNESS_RANGE_MULTIPLIER_DATA.applyIfAbsent(mob, blindnessRangeMultiplier);
	}

	@SubscribeEvent
	public void onTargetDistanceMultiplier(LivingEvent.LivingVisibilityEvent event) {
		if (!this.isEnabled()
				|| !BLINDNESS_RANGE_MULTIPLIER_DATA.has(event.getLookingEntity())
				|| !(event.getLookingEntity() instanceof LivingEntity livingEntity)
				|| !livingEntity.hasEffect(MobEffects.BLINDNESS))
			return;

		event.modifyVisibility(BLINDNESS_RANGE_MULTIPLIER_DATA.get(livingEntity));
	}

	@SubscribeEvent
	public void onBlindnessApply(MobEffectEvent.Added event) {
		if (!this.isEnabled()
				|| event.getEffectInstance().getEffect() != MobEffects.BLINDNESS
				|| !BLINDNESS_RANGE_MULTIPLIER_DATA.has(event.getEntity()))
			return;

		MCUtils.applyModifier(event.getEntity(), Attributes.FOLLOW_RANGE, BLINDNESS_FOLLOW_RANGE_ID, BLINDNESS_RANGE_MULTIPLIER_DATA.get(event.getEntity()) - 1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, true);
	}

	@SubscribeEvent
	public void onBlindnessRemove(MobEffectEvent.Remove event) {
		if (!this.isEnabled()
				|| event.getEffectInstance() == null
				|| event.getEffectInstance().getEffect() != MobEffects.BLINDNESS
				|| event.getEntity().getAttribute(Attributes.FOLLOW_RANGE) == null)
			return;

		event.getEntity().getAttribute(Attributes.FOLLOW_RANGE).removeModifier(BLINDNESS_FOLLOW_RANGE_ID);
	}
}
