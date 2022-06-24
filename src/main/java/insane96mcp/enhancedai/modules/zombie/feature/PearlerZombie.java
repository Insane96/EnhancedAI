package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.zombie.ai.PearlUseGoal;
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

@Label(name = "Pearler Zombie", description = "Let zombies use ender pearls. Either put ender pearls in main or off hand and when far enough from the player they will throw it.")
public class PearlerZombie extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> equipEnderPearlChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> enderPearlAmountConfig;
	private final ForgeConfigSpec.IntValue inaccuracyConfig;

	private final Blacklist.Config entityBlacklistConfig;

	public double equipEnderPearlChance = 0.05;
	public int enderPearlAmount = 2;
	public int inaccuracy = 4;

	public Blacklist entityBlacklist;

	public PearlerZombie(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		equipEnderPearlChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with an ender pearl in the off hand. I highly recommend using something like Mobs Properties Randomness to have more control over mobs equipment.")
				.defineInRange("Equip Ender Pearl Chance", this.equipEnderPearlChance, 0d, 1d);
		enderPearlAmountConfig = Config.builder
				.comment("How many ender pearls will Zombies spawn with.")
				.defineInRange("Ender Pearl Amount", this.enderPearlAmount, 1, 16);
		inaccuracyConfig = Config.builder
				.comment("Inaccuracy when throwing the ender pearl.")
				.defineInRange("Inaccuracy", this.inaccuracy, 1, 128);

		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Pearler AI")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.equipEnderPearlChance = this.equipEnderPearlChanceConfig.get();
		this.enderPearlAmount = this.enderPearlAmountConfig.get();
		this.inaccuracy = this.inaccuracyConfig.get();

		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (event.getWorld().isClientSide)
			return;

		if (!(event.getEntity() instanceof Zombie zombie))
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(zombie))
			return;

		if (event.getWorld().random.nextDouble() < this.equipEnderPearlChance)
			zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL, this.enderPearlAmount));

		zombie.goalSelector.addGoal(2, new PearlUseGoal(zombie));
	}
}
