package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.zombie.ai.FishingTargetGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Fisher Zombie", description = "Let zombies use Fishing Rods, reeling players in. Either put a Fishing Rod in main or off hand and when near enough from the player they will throw it.")
public class FisherZombie extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> equipFishingRodChanceConfig;
	private final Blacklist.Config entityBlacklistConfig;

	public double equipFishingRodChance = 0.04;
	public Blacklist entityBlacklist;

	public FisherZombie(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		equipFishingRodChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with a Fishing Rod in the off hand. I highly recommend using something like Mobs Properties Randomness to have more control over mobs equipment.")
				.defineInRange("Equip Fishing Rod Chance", this.equipFishingRodChance, 0d, 1d);
		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Fisher AI")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.equipFishingRodChance = this.equipFishingRodChanceConfig.get();
		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Zombie zombie))
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(zombie))
			return;

		if (event.getWorld().random.nextDouble() < this.equipFishingRodChance)
			zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.FISHING_ROD));

		zombie.goalSelector.addGoal(2, new FishingTargetGoal(zombie));
	}
}
