package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

@Label(name = "Attacking")
public class Attacking extends Feature {

	private final ForgeConfigSpec.BooleanValue meleeAttacksAttributeBasedConfig;

	public boolean meleeAttacksAttributeBased = true;

	public Attacking(Module module) {
		super(Config.builder, module, true, false);
		super.pushConfig(Config.builder);
		meleeAttacksAttributeBasedConfig = Config.builder
				.comment("If true melee monsters (zombies, etc) will attack based off the forge:attack_range attribute. Increasing it will make mobs attack for farther away.")
				.define("Melee ATtacks Attribute Based", this.meleeAttacksAttributeBased);
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

			event.add(entityType, ForgeMod.ATTACK_RANGE.get(), entityType.getWidth());
		}
	}
}
