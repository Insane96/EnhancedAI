package insane96mcp.enhancedai.modules.zombie.entity.projectile;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Biting Zombie", description = "Zombies can bite the player if are attacked with non-weapons")
@LoadFeature(module = Modules.Ids.ZOMBIE)
public class BitingZombie extends Feature {

	@Config(min = 0d, max = 1d)
	@Label(name = "Chance", description = "Chance for a Zombie to bite a player")
	public static Double chance = 0.4d;
	@Config(min = 0d)
	@Label(name = "Damage Multiplier", description = "The damage dealt to the player is the same as the zombie attack damage, multiplied by this")
	public static Double damageMultiplier = 1d;
	@Config
	@Label(name = "Entity Blacklist", description = "Entities in this list will not be affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public BitingZombie(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerAttack(LivingDamageEvent event) {
		if (!this.isEnabled()
		 		|| event.getEntity().getLevel().isClientSide
				|| !(event.getEntity() instanceof Zombie zombie)
		 		|| entityBlacklist.isEntityBlackOrNotWhitelist(zombie)
				|| zombie.getAttribute(Attributes.ATTACK_DAMAGE) == null
				|| !(event.getSource().getDirectEntity() instanceof Player player)
				|| !player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))
			return;

		if (zombie.getRandom().nextDouble() < chance) {
			DamageSource damageSource = zombie.damageSources().mobAttack(zombie);
			player.hurt(damageSource, (float) zombie.getAttribute(Attributes.ATTACK_DAMAGE).getValue() * damageMultiplier.floatValue());
		}
	}
}
