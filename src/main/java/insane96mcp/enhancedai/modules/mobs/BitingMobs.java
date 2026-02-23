package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Mobs can bite if are attacked with non-weapons. Only mobs in the enhancedai:mobs/biting_mobs/can_bite entity type tag can bite. Entity types in `mobs/biting_mobs/unaffected_by_bite` tag are unaffected by bites. Damage types in `biting_mobs/doesnt_trigger_bite` tag don't trigger biting mobs.")
public class BitingMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BITE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/biting_mobs/can_bite"));
	public static final TagKey<EntityType<?>> UNAFFECTED_BY_BITE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/biting_mobs/unaffected_by_bite"));
	public static final TagKey<DamageType> DOESNT_TRIGGER_BITE = TagKey.create(Registries.DAMAGE_TYPE, EnhancedAI.location("doesnt_trigger_bite"));
	ResourceKey<DamageType> BITE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, EnhancedAI.location("bite"));

	@Config(min = 0d, max = 1d, description = "Chance for a Mob to bite the attacker")
	public static DifficultyBasedConfig chance = new DifficultyBasedConfig(0.2d, 0.2d, 0.3d);
	@Config(min = 0d, description = "The damage dealt to the attacker when bit")
	public static Double damage = 3d;

	public static EAIData<Double> CHANCE;
	public static EAIData<Double> DAMAGE;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		CHANCE = EAIData.ofDouble(this.createDataKey("chance"));
		DAMAGE = EAIData.ofDouble(this.createDataKey("damage"));
	}

	@SubscribeEvent
	public void onPlayerAttack(LivingDamageEvent.Pre event) {
		if (!this.isEnabled()
		 		|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| event.getSource().is(DOESNT_TRIGGER_BITE)
				|| !mob.getType().is(CAN_BITE)
				|| mob.getAttribute(Attributes.ATTACK_DAMAGE) == null
				|| !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
				|| attacker.getType().is(UNAFFECTED_BY_BITE)
				|| attacker.getMainHandItem().getAttributeModifiers().modifiers().stream()
						.anyMatch(e -> e.attribute().value() == Attributes.ATTACK_DAMAGE.value()))
			return;

		if (mob.getRandom().nextDouble() < CHANCE.get(mob)) {
			DamageSource damageSource = mob.damageSources().source(BITE_DAMAGE_TYPE, mob);
			if (attacker.isInvulnerableTo(damageSource))
				return;
			attacker.hurt(damageSource, DAMAGE.get(mob).floatValue());
		}
	}

	@SubscribeEvent
	public void onJoinLevelEvent(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_BITE))
			return;

		CHANCE.applyIfAbsent(mob, chance.getByDifficulty(mob.level()));
		DAMAGE.applyIfAbsent(mob, damage);
	}
}
