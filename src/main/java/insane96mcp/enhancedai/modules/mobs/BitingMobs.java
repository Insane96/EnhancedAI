package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Biting Mobs", description = "Mobs can bite if are attacked with non-weapons")
@LoadFeature(module = Modules.Ids.MOBS)
public class BitingMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BITE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_bite"));
	ResourceKey<DamageType> BITE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "zombie_bite"));

	@Config(min = 0d, max = 1d)
	@Label(name = "Chance", description = "Chance for a Mob to bite the attacker")
	public static Difficulty chance = new Difficulty(0.2d, 0.2d, 0.3d);
	@Config(min = 0d)
	@Label(name = "Damage", description = "The damage dealt to the attacker when bit")
	public static Double damage = 3d;

	public BitingMobs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerAttack(LivingDamageEvent event) {
		if (!this.isEnabled()
		 		|| event.getEntity().level().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_BITE)
				|| mob.getAttribute(Attributes.ATTACK_DAMAGE) == null
				|| !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
				|| attacker.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))
			return;

		if (mob.getRandom().nextDouble() < chance.getByDifficulty(event.getEntity().level())) {
			DamageSource damageSource = mob.damageSources().source(BITE_DAMAGE_TYPE,  mob);
			attacker.hurt(damageSource, damage.floatValue());
		}
	}
}
