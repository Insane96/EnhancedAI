package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

@Label(name = "Attacking", description = "Changes attack range to use the 1.20.2 mechanic")
@LoadFeature(module = Modules.Ids.BASE)
public class Attacking extends Feature {

	@Config
	@Label(name = "Melee Attacks Attribute Based", description = "If true melee monsters (zombies, etc) will attack based off the forge:attack_range attribute instead of a fixed ~0.82 blocks. By default, mobs' forge:attack_range is set to 0.82 blocks, like vanilla 1.20.2.")
	public static Boolean meleeAttacksAttributeBased = false;

	public Attacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void attackRangeAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, ForgeMod.ATTACK_RANGE.get()))
				continue;

			event.add(entityType, ForgeMod.ATTACK_RANGE.get(), DEFAULT_ATTACK_REACH);
		}
	}

	public static Boolean shouldChangeAttackRange() {
		return isEnabled(Attacking.class) && meleeAttacksAttributeBased;
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
			attackReach = attacker.getAttributeValue(ForgeMod.ATTACK_RANGE.get());
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
