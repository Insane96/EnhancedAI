package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

@Label(name = "Attacking")
@LoadFeature(module = Modules.Ids.BASE)
public class Attacking extends Feature {

	@Config
	@Label(name = "Melee Attacks Attribute Based", description = "If true melee monsters (zombies, etc) will attack based off the forge:attack_range attribute. Increasing it will make mobs attack for farther away. Be aware that the attack doesn't check if there are block between the target and the mob so might result in mobs attacking through walls (like spiders already do in vanilla)")
	public static Boolean meleeAttacksAttributeBased = false;

	@Config
	@Label(name = "Attack Speed.Enabled", description = "If true melee monsters (zombies, etc) attack rate is defined by their attack speed -40%, minumum once every 0.5 seconds with no weapon. This effectively buffs any mob that has no weapon.")
	public static Boolean meleeAttackSpeedBased = true;
	@Config(min = 0d, max = 4d)
	@Label(name = "Attack Speed.Multiplier", description = "Multiplies the attack speed of monsters by this value. E.g. 0.6 means that mobs attack 40% slower than the player with the same equipment")
	public static Difficulty attackSpeedMultiplier = new Difficulty(0.4d, 0.5d, 0.6d);
	@Config(min = 0f, max = 4f)
	@Label(name = "Attack Speed.Maximum", description = "The maximum attack speed a mob can attack with (in attacks per second, 2 is an attach every 0.5 seconds, 1.25 is an attack every 0.8s, 1 is an attack every 1s).")
	public static Double attackSpeedMaximum = 1.25d;

	public Attacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void attributeModificationEvent(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, ForgeMod.ENTITY_REACH.get()))
				event.add(entityType, ForgeMod.ENTITY_REACH.get(), entityType.getWidth() * 2d);
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
}
