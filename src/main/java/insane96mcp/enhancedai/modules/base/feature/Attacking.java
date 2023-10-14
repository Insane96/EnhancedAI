package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

@Label(name = "Attacking", description = "Changes attack range to use the 1.20.2 mechanic")
public class Attacking extends Feature {

	private final ForgeConfigSpec.BooleanValue meleeAttacksAttributeBasedConfig;

	public boolean meleeAttacksAttributeBased = true;

	public Attacking(Module module) {
		super(Config.builder, module, true);
		this.pushConfig(Config.builder);
		meleeAttacksAttributeBasedConfig = Config.builder
				.comment("If true melee monsters (zombies, etc) will attack based off the forge:attack_range attribute instead of a fixed ~0.82 blocks. By default, mobs' forge:attack_range is set to 0.82 blocks, like vanilla 1.20.2.")
				.define("Melee Attacks Attribute Based", this.meleeAttacksAttributeBased);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.meleeAttacksAttributeBased = this.meleeAttacksAttributeBasedConfig.get();
	}

	public static void attackRangeAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, ForgeMod.ATTACK_RANGE.get()))
				continue;

			event.add(entityType, ForgeMod.ATTACK_RANGE.get(), DEFAULT_ATTACK_REACH);
		}
	}

	public boolean shouldChangeAttackRange() {
		return this.isEnabled() && this.meleeAttacksAttributeBased;
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
		if (Modules.base.attacking.shouldChangeAttackRange())
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
