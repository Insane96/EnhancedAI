package insane96mcp.enhancedai.modules.zombie.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.zombie.ai.DiggingGoal;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Digger Zombie", description = "Zombies can mine blocks to reach the target. Uses offhand item to mine")
@LoadFeature(module = Modules.Ids.ZOMBIE)
public class DiggerZombie extends Feature {
	@Config(min = 0d, max = 1d)
	@Label(name = "Digger Chance", description = "Chance for a Zombie to spawn with the digger ability")
	public static Double diggerChance = 0.07d;
	@Config
	@Label(name = "Digger Tool Only", description = "Zombies with Digger AI will mine only if they have any tool in the off-hand")
	public static Boolean diggerToolOnly = true;
	@Config
	@Label(name = "Digger Proper Tool Only", description = "Zombies with Digger AI will mine only if their off-hand tool can mine targeted blocks (e.g. zombies with shovels will not mine stone). Blocks that require no tool (e.g. planks) will be minable regardless of proper tool or not.")
	public static Boolean diggerProperToolOnly = false;
	@Config
	@Label(name = "Equip Wooden Pick", description = "Zombies with Digger AI will spawn with a Wooden Pickaxe.")
	public static Boolean equipWoodenPick = true;
	@Config(min = -64, max = 320)
	@Label(name = "Max Y Dig", description = "The maximum Y coordinate at which Zombies can mine.")
	public static Integer maxYDig = 320;
	@Config(min = 0, max = 128)
	@Label(name = "Max Distance", description = "The maximum distance from the target at which the zombie can mine. Set to 0 to always mine.")
	public static Integer maxDistance = 0;
	@Config
	@Label(name = "Blacklist Tile Entities", description = "Zombies with Digger AI will not be able to break tile entities")
	public static Boolean blacklistTileEntities = false;
	@Config(min = 0d, max = 128d)
	@Label(name = "Digger Speed Multiplier", description = "Multiplier for digger zombies mining speed. E.g. with this set to 2, zombies will take twice the time to mine a block.")
	public static Double miningSpeedMultiplier = 1.5d;
	@Config
	@Label(name = "Block Blacklist", description = "Blocks in here will not be minable by zombies (or will be the only minable in case it's whitelist)")
	public static Blacklist blockBlacklist = new Blacklist(Collections.emptyList(), false);
	@Config
	@Label(name = "Entity Blacklist", description = "Entities in this list will not be affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public DiggerZombie(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
		 		|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Zombie zombie)
		 		|| entityBlacklist.isEntityBlackOrNotWhitelist(zombie))
			return;

		CompoundTag persistentData = zombie.getPersistentData();

		boolean miner = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Zombie.MINER, zombie.getRandom().nextDouble() < diggerChance);
		boolean diggerToolOnly1 = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Zombie.TOOL_ONLY, diggerToolOnly);
		boolean diggerProperToolOnly1 = NBTUtils.getBooleanOrPutDefault(persistentData, EAStrings.Tags.Zombie.PROPER_TOOL_ONLY, diggerProperToolOnly);

		if (miner) {
			zombie.goalSelector.addGoal(1, new DiggingGoal(zombie, maxDistance, diggerToolOnly1, diggerProperToolOnly1));
			if (equipWoodenPick && zombie.getOffhandItem().isEmpty())
			{
				zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.WOODEN_PICKAXE));
				zombie.setDropChance(EquipmentSlot.OFFHAND, -1f);
			}
		}
	}
}
