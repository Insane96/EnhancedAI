package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Changes mobs attack range to use the 1.20.2 mechanic. Only entities in the `enhancedai:mobs/melee_attacking` entity type tag will be affected.")
public class MeleeAttacking extends Feature {
	public static final TagKey<EntityType<?>> CHANGE_MELEE_ATTACKING = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/melee_attacking"));

	//Directly stolen from 1.20.2
	private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - (double)0.6F;

	@Config(description = "If true melee monsters (zombies, etc) will attack like 1.20.2+ and the attack range is based off the forge:entity_reach attribute (default 0.82 blocks).")
	public static Boolean attackReachAttributeBased = true;

	@Config(description = "If true melee monsters (zombies, etc) attack speed is 4 (like the player) and will be based off the `minecraft:generic.attack_speed` attribute.")
	public static Boolean attackSpeed$attributeBased = true;
	@Config(min = 0d, max = 4d, description = "Multiplies the attack speed of monsters by this value.")
	public static DifficultyBasedConfig attackSpeed$multiplier = new DifficultyBasedConfig(0.25d, 0.25d, 0.25d);
	@Config(min = 0f, max = 4f, description = "The maximum attack speed a mob can attack with (in attacks per second, 2 is an attack every 0.5 seconds, 1.25 is an attack every 0.8s, 1 is an attack every 1s).")
	public static Double attackSpeed$maximum = 4d;

	public static void attributeModificationEvent(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, Attributes.ENTITY_INTERACTION_RANGE))
				event.add(entityType, Attributes.ENTITY_INTERACTION_RANGE, DEFAULT_ATTACK_REACH);
			if (!event.has(entityType, Attributes.ATTACK_SPEED))
				event.add(entityType, Attributes.ATTACK_SPEED, 4d);
		}
	}

	public static Boolean shouldChangeAttackReach() {
		return isEnabled(MeleeAttacking.class) && attackReachAttributeBased;
	}

	public static Boolean shouldUseAttackSpeedAttribute() {
		return isEnabled(MeleeAttacking.class) && attackSpeed$attributeBased;
	}

	public static Boolean shouldBeAffectedByFeature(LivingEntity attacker) {
		return isEnabled(MeleeAttacking.class) && !Spawning.isUnaffectedByFeatures(attacker) && attacker.getType().is(CHANGE_MELEE_ATTACKING);
	}

	public static boolean isWithinMeleeAttackRange(LivingEntity attacker, LivingEntity attacked) {
		return getAttackBoundingBox(attacker).intersects(getHitbox(attacked));
	}

	protected static AABB getAttackBoundingBox(LivingEntity attacker) {
		Entity entity = attacker.getVehicle();
		AABB aabb;
		if (entity != null) {
			AABB veichleAABB = entity.getBoundingBox();
			AABB attackerAABB = attacker.getBoundingBox();
			aabb = new AABB(Math.min(attackerAABB.minX, veichleAABB.minX), attackerAABB.minY, Math.min(attackerAABB.minZ, veichleAABB.minZ), Math.max(attackerAABB.maxX, veichleAABB.maxX), attackerAABB.maxY, Math.max(attackerAABB.maxZ, veichleAABB.maxZ));
		}
		else {
			aabb = attacker.getBoundingBox();
		}

		double attackReach = DEFAULT_ATTACK_REACH;
		if (MeleeAttacking.shouldChangeAttackReach())
			attackReach = attacker.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
		return aabb.inflate(attackReach, 0.0D, attackReach);
	}

	protected static AABB getHitbox(LivingEntity entity) {
		AABB aabb = entity.getBoundingBox();
		Entity vehicle = entity.getVehicle();
		if (vehicle != null) {
			Vec3 vec3 = new Vec3(entity.getX(), vehicle.getPassengerRidingPosition(entity).y, entity.getY());
			return aabb.setMinY(Math.max(vec3.y, aabb.minY));
		}
		else {
			return aabb;
		}
	}
}
