package insane96mcp.enhancedai.modules.base;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

@Label(name = "Attacking", description = "Changes attack range to use the 1.20.2 mechanic")
@LoadFeature(module = Modules.Ids.BASE)
public class Attacking extends Feature {

	@Config
	@Label(name = "Melee Attacks Attribute Based", description = "If true melee monsters (zombies, etc) will attack based off the forge:entity_reach attribute instead of a fixed ~0.82 blocks. By default, mobs' forge:attack_range is set to 0.82 blocks, like vanilla 1.20.2.")
	public static Boolean meleeAttacksAttributeBased = false;

	@Config
	@Label(name = "Attack Speed.Enabled", description = "If true melee monsters (zombies, etc) attack rate is defined by their attack speed -40%, minumum once every 0.5 seconds with no weapon. This effectively buffs any mob that has no weapon.")
	public static Boolean meleeAttackSpeedBased = true;
	@Config(min = 0d, max = 4d)
	@Label(name = "Attack Speed.Multiplier", description = "Multiplies the attack speed of monsters by this value. E.g. 0.6 means that mobs attack 40% slower than the player with the same equipment")
	public static Difficulty attackSpeedMultiplier = new Difficulty(0.4d, 0.5d, 0.6d);
	@Config(min = 0f, max = 4f)
	@Label(name = "Attack Speed.Maximum", description = "The maximum attack speed a mob can attack with (in attacks per second, 2 is an attach every 0.5 seconds, 1.25 is an attack every 0.8s, 1 is an attack every 1s). In vanilla mobs have 1 attack speed.")
	public static Double attackSpeedMaximum = 1.25d;

	public Attacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void attributeModificationEvent(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, ForgeMod.ENTITY_REACH.get()))
				event.add(entityType, ForgeMod.ENTITY_REACH.get(), DEFAULT_ATTACK_REACH);
			if (!event.has(entityType, Attributes.ATTACK_SPEED))
				event.add(entityType, Attributes.ATTACK_SPEED, 4d);
		}
	}

	public static Boolean shouldChangeAttackRange() {
		return isEnabled(Attacking.class) && meleeAttacksAttributeBased;
	}

	public static Boolean shouldUseAttackSpeedAttribute() {
		return isEnabled(Attacking.class) && meleeAttackSpeedBased;
	}

	//Directly stolen from 1.20.2
	private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - (double)0.6F;

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
		if (Attacking.shouldChangeAttackRange())
			attackReach = attacker.getAttributeValue(ForgeMod.ENTITY_REACH.get());
		return aabb.inflate(attackReach, 0.0D, attackReach);
	}

	protected static AABB getHitbox(LivingEntity entity) {
		AABB aabb = entity.getBoundingBox();
		Entity veichle = entity.getVehicle();
		if (veichle != null) {
			Vec3 vec3 = new Vec3(entity.getX(), veichle.getPassengersRidingOffset(), entity.getY());
			return aabb.setMinY(Math.max(vec3.y, aabb.minY));
		}
		else {
			return aabb;
		}
	}
}
