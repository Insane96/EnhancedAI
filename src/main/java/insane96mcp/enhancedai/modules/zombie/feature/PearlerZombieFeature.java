package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.zombie.ai.AIZombiePearler;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;

@Label(name = "Pearler Zombie", description = "Let zombies use ender pearls. Either put ender pearls in main or off hand and when far enough from the player they will throw it.")
public class PearlerZombieFeature extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> equipEnderPearlChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> enderPearlAmountConfig;
	private final BlacklistConfig entityBlacklistConfig;

	public double equipEnderPearlChance = 0.04;
	public int enderPearlAmount = 2;
	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public PearlerZombieFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		equipEnderPearlChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with an ender pearl in the off hand. I highly recommend using something like Mobs Properties Randomness to have more control over mobs equipment.")
				.defineInRange("Equip Ender Pearl Chance", this.equipEnderPearlChance, 0d, 1d);
		enderPearlAmountConfig = Config.builder
				.comment("How many ender pearls will Zombies spawn with.")
				.defineInRange("Ender Pearl Amount", this.enderPearlAmount, 1, 16);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Pearler AI", Collections.emptyList(), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.equipEnderPearlChance = this.equipEnderPearlChanceConfig.get();
		this.enderPearlAmount = this.enderPearlAmountConfig.get();
		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof Zombie zombie))
			return;

		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
			if (blacklistEntry.matchesEntity(zombie)) {
				if (!this.entityBlacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}
		if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
			return;

		if (event.getWorld().random.nextDouble() < this.equipEnderPearlChance)
			zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL, this.enderPearlAmount));

		zombie.goalSelector.addGoal(2, new AIZombiePearler(zombie));
	}
}
