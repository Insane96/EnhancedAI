package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.zombie.ai.PearlUseGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

@Label(name = "Pearler Zombie", description = "Let zombies use ender pearls. Either put ender pearls in main or off hand and when far enough from the player they will throw it.")
@LoadFeature(module = Modules.Ids.ZOMBIE)
public class PearlerZombie extends Feature {
	@Config(min = 0d, max = 1d)
	@Label(name = "Equip Ender Pearl Chance", description = "Chance for a Zombie to spawn with an ender pearl in the offhand. I highly recommend using something like Mobs Properties Randomness to have more control over mobs equipment.")
	public static Double equipEnderPearlChance = 0.05;
	@Config(min = 0, max = 16)
	@Label(name = "Ender Pearl Amount", description = "How many ender pearls will Zombies spawn with.")
	public static Integer enderPearlAmount = 2;
	@Config(min = 1, max = 128)
	@Label(name = "Inaccuracy", description = "Inaccuracy when throwing the ender pearl.")
	public static Integer inaccuracy = 4;
	@Config(min = 1, max = 128)
	@Label(name = "Entity Blacklist", description = "Entities that will not be affected by this module.")
	public static Blacklist entityBlacklist = new Blacklist(List.of(
			IdTagMatcher.newId("quark:forgotten")
	), false);

	public PearlerZombie(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Zombie zombie)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(zombie))
			return;

		if (zombie.getOffhandItem().isEmpty() && zombie.getRandom().nextDouble() < equipEnderPearlChance)
			zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL, enderPearlAmount));

		zombie.goalSelector.addGoal(2, new PearlUseGoal(zombie));
	}
}
