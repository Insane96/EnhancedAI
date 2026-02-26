package insane96mcp.enhancedai.module.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@LoadFeature(module = EAIModules.MOBS, description = "Changes mobs attacks to use the attack speed attribute. Only entities in the `enhancedai:mobs/melee_attacking` entity type tag will be affected.")
public class MeleeAttacking extends Feature {
	public static final TagKey<EntityType<?>> CHANGE_MELEE_ATTACKING = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/melee_attacking"));

	@Config(description = "If true melee monsters (zombies, etc) attack speed is 4 (like the player) and will be based off the `minecraft:generic.attack_speed` attribute.")
	public static Boolean attackSpeed$attributeBased = true;
	@Config(min = 0d, max = 4d, description = "Multiplies the attack speed of monsters by this value.")
	public static DifficultyBasedConfig attackSpeed$multiplier = new DifficultyBasedConfig(0.25d, 0.25d, 0.25d);
	@Config(min = 0f, max = 4f, description = "The maximum attack speed a mob can attack with (in attacks per second, 2 is an attack every 0.5 seconds, 1.25 is an attack every 0.8s, 1 is an attack every 1s).")
	public static Double attackSpeed$maximum = 4d;

	public static void attributeModificationEvent(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, Attributes.ATTACK_SPEED))
				event.add(entityType, Attributes.ATTACK_SPEED, 4d);
		}
	}

	public static Boolean shouldUseAttackSpeedAttribute() {
		return isEnabled(MeleeAttacking.class) && attackSpeed$attributeBased;
	}

	public static Boolean shouldBeAffectedByFeature(LivingEntity attacker) {
		return isEnabled(MeleeAttacking.class) && !Spawning.isUnaffectedByFeatures(attacker) && attacker.getType().is(CHANGE_MELEE_ATTACKING);
	}
}
