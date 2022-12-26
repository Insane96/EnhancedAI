package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.zombie.ai.FishingTargetGoal;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Fisher Zombie", description = "Let zombies use Fishing Rods, reeling players in. Either put a Fishing Rod in main or off hand and when near enough from the player they will throw it.")
@LoadFeature(module = Modules.Ids.ZOMBIE)
public class FisherZombie extends Feature {

	@Config(min = 0d, max = 1d)
	@Label(name = "Equip Fishing Rod Chance", description = "Chance for a Zombie to spawn with a Fishing Rod in the offhand. I highly recommend using something like Mobs Properties Randomness to have more control over mobs equipment.")
	public static Double equipFishingRodChance = 0.07;
	@Config
	@Label(name = "Entity Blacklist", description = "Chance for a Zombie to spawn with a Fishing Rod in the off hand. I highly recommend using something like Mobs Properties Randomness to have more control over mobs equipment.")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public FisherZombie(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Zombie zombie)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(zombie))
			return;

		if (zombie.getRandom().nextDouble() < equipFishingRodChance)
			zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.FISHING_ROD));

		zombie.goalSelector.addGoal(2, new FishingTargetGoal(zombie));
	}
}
