package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.zombie.ai.DiggingGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Digger Zombie", description = "Zombies can mine blocks to reach the target. Uses offhand item to mine")
public class DiggerZombie extends Feature {
	private final ForgeConfigSpec.ConfigValue<Double> diggerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> diggerToolOnlyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> diggerProperToolOnlyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> equipWoodenPickConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxYDigConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxDistanceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> blacklistTileEntitiesConfig;
	private final ForgeConfigSpec.ConfigValue<Double> miningSpeedMultiplierConfig;
	private final Blacklist.Config blockBlacklistConfig;
	private final Blacklist.Config entityBlacklistConfig;

	public double diggerChance = 0.07d;
	public boolean diggerToolOnly = false;
	public boolean diggerProperToolOnly = false;
	public boolean equipWoodenPick = true;
	public int maxYDig = 64;
	public int maxDistance = 0;
	public boolean blacklistTileEntities = false;
	public double miningSpeedMultiplier = 1d;
	public Blacklist blockBlacklist;
	public Blacklist entityBlacklist;

	public DiggerZombie(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		diggerChanceConfig = Config.builder
				.comment("Chance for a Zombie to spawn with the digger ability")
				.defineInRange("Digger Chance", this.diggerChance, 0d, 1d);
		diggerToolOnlyConfig = Config.builder
				.comment("Zombies with Digger AI will mine only if they have a tool in the off-hand")
				.define("Digger Tool Only", this.diggerToolOnly);
		diggerProperToolOnlyConfig = Config.builder
				.comment("Zombies with Digger AI will mine only if their off-hand tool can mine targeted blocks (e.g. zombies with shovels will not mine stone). Blocks that require no tool (e.g. planks) will be minable.")
				.define("Digger Proper Tool Only", this.diggerProperToolOnly);
		equipWoodenPickConfig = Config.builder
				.comment("Zombies with Digger AI will spawn with a Wooden Pickaxe.")
				.define("Equip Wooden Pick", this.equipWoodenPick);
		maxYDigConfig = Config.builder
				.comment("The maximum Y coordinate at which Zombies can mine.")
				.defineInRange("Max Y Dig", this.maxYDig, -128, 512);
		maxDistanceConfig = Config.builder
				.comment("The maximum distance from the target at which the zombie can mine. Set to 0 to always mine.")
				.defineInRange("Max Distance", this.maxDistance, 0, 128);
		miningSpeedMultiplierConfig = Config.builder
				.comment("Multiplier for digger zombies mining speed. E.g. with this set to 2, zombies will take twice the time to mine a block.")
				.defineInRange("Digger Speed Multiplier", this.miningSpeedMultiplier, 0d, 128d);
		blacklistTileEntitiesConfig = Config.builder
				.comment("Zombies with Digger AI will not be able to break tile entities")
				.define("Blacklist Tile Entities", this.blacklistTileEntities);
		blockBlacklistConfig = new Blacklist.Config(Config.builder, "Block Blacklist", "Blocks in here will not be minable by zombies (or will be the only minable in case it's whitelist)")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Digger AI")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.diggerChance = this.diggerChanceConfig.get();
		this.diggerToolOnly = this.diggerToolOnlyConfig.get();
		this.diggerProperToolOnly = this.diggerProperToolOnlyConfig.get();
		this.equipWoodenPick = this.equipWoodenPickConfig.get();
		this.maxYDig = this.maxYDigConfig.get();
		this.maxDistance = this.maxDistanceConfig.get();
		this.miningSpeedMultiplier = this.miningSpeedMultiplierConfig.get();
		this.blacklistTileEntities = this.blacklistTileEntitiesConfig.get();
		this.blockBlacklist = this.blockBlacklistConfig.get();
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

		boolean miner = zombie.level.random.nextDouble() < this.diggerChance;

		CompoundTag persistentData = zombie.getPersistentData();

		if (persistentData.contains(Strings.Tags.Zombie.MINER)) {
			miner = persistentData.getBoolean(Strings.Tags.Zombie.MINER);
		}
		else {
			persistentData.putBoolean(Strings.Tags.Zombie.MINER, miner);
		}

		if (miner) {
			zombie.goalSelector.addGoal(1, new DiggingGoal(zombie, this.maxDistance, this.diggerToolOnly, this.diggerProperToolOnly));
			if (this.equipWoodenPick)
			{
				zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WOODEN_PICKAXE));
				zombie.setDropChance(EquipmentSlot.OFFHAND, -1f);
			}
		}
	}
}
