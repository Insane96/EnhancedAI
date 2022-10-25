package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

@Label(name = "Attacking")
@LoadFeature(module = Modules.Ids.BASE)
public class Attacking extends Feature {

	@Config
	@Label(name = "Melee Attacks Attribute Based", description = "If true melee monsters (zombies, etc) will attack based off the forge:attack_range attribute. Increasing it will make mobs attack for farther away. Be aware that the attack doesn't check if there are block between the target and the mob so might result in mobs attacking through walls (like spiders already do in vanilla)")
	public static Boolean meleeAttacksAttributeBased = false;

	public Attacking(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static void attackRangeAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, ForgeMod.ATTACK_RANGE.get()))
				continue;

			event.add(entityType, ForgeMod.ATTACK_RANGE.get(), entityType.getWidth() * 2d);
		}
	}

	public static Boolean shouldChangeAttackRange() {
		return isEnabled(Attacking.class) && meleeAttacksAttributeBased;
	}
}
