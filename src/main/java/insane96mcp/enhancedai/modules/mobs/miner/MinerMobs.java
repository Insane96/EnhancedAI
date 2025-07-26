package insane96mcp.enhancedai.modules.mobs.miner;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.setup.NBTUtils;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Mobs can mine blocks to reach the target. Uses offhand item to mine. Only mobs in the entity type tag enhancedai:can_be_miner can spawn with the ability to mine and blocks in the tag enhancedai:miner_block_blacklist cannot be mined.")
public class MinerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BE_MINER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_be_miner"));
	public static final TagKey<Block> BLOCK_BLACKLIST = TagKey.create(Registries.BLOCK, EnhancedAI.location("miner_block_blacklist"));
	public static final String MINER = EnhancedAI.RESOURCE_PREFIX + "miner";
	public static final String TIME_TO_BREAK_MULTIPLIER = EnhancedAI.RESOURCE_PREFIX + "time_to_break_multiplier";
	public static final String TOOL_ONLY = EnhancedAI.RESOURCE_PREFIX + "tool_only";
	public static final String PROPER_TOOL_ONLY = EnhancedAI.RESOURCE_PREFIX + "proper_tool_only";
	public static final String ALWAYS_REQUIRE_PROPER_TOOL = EnhancedAI.RESOURCE_PREFIX + "always_require_proper_tool";

	@Config(min = 0d, max = 1d, description = "Chance for a mob in the entity type tag enhancedai:can_be_miner to spawn with the miner ability")
	public static Double minerChance = 0.07d;
	@Config(description = "Mobs with the miner AI will mine only if they have any tool in the off-hand")
	public static Boolean canMineWithToolOnly = true;
	@Config(description = "Mobs with the miner AI will mine only if their off-hand tool can mine targeted blocks (e.g. zombies with shovels will not mine stone). Blocks that require no tool (e.g. planks) will be minable regardless of proper tool or not.")
	public static Boolean canMineWithProperToolOnly = false;
	@Config(description = "If 'Can mine with proper tool only' is enabled, mobs with the miner AI will mine blocks that don't require a tool only with the proper tool.")
	public static Boolean alwaysRequireProperTool = false;
	@Config(description = "Mobs with Miner AI will spawn with a Stone Pickaxe that never drops.")
	public static Boolean equipStonePick = true;
	@Config(min = -512, max = 1024, description = "Mobs can mine from the bottom of the world to this Y level.")
	public static Integer maxY = 320;
	@Config(min = 0, max = 128, description = "The maximum distance from the target at which the Mobs can mine. Set to 0 to always mine.")
	public static Integer maxDistance = 0;
	@Config(description = "Mobs with Miner AI will not be able to break tile entities")
	public static Boolean blacklistTileEntities = false;
	@Config(min = 0d, max = 128d, description = "Multiplier for the time a mob takes to break blocks. E.g. with this set to 2, mobs will take twice the time to mine a block.")
	public static Double timeToBreakMultiplier = 1.25d;
	@Config(description = "If true, the block tag `enhancedai:miner_block_blacklist` will be treated as a whitelist instead of blacklist")
	public static Boolean blockBlacklistAsWhitelist = false;
	@Config(description = "Dimensions where mobs can't spawn with the ability to mine.")
	public static Blacklist dimensionBlacklist = new Blacklist();

	public MinerMobs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
		 		|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
		 		|| !mob.getType().is(CAN_BE_MINER)
				|| MinerMobs.isDimensionBlackOrNotWhitelisted(event.getLevel()))
			return;

		CompoundTag persistentData = mob.getPersistentData();

		boolean miner = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, MINER, mob.getRandom().nextDouble() < minerChance);
		double timeToBreakMultiplier1 = NBTUtils.getDoubleOrPutDefaultLegacy(persistentData, TIME_TO_BREAK_MULTIPLIER, timeToBreakMultiplier);
		boolean toolOnly = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, TOOL_ONLY, canMineWithToolOnly);
		boolean properToolOnly = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, PROPER_TOOL_ONLY, canMineWithProperToolOnly);
		boolean properToolRequired = NBTUtils.getBooleanOrPutDefaultLegacy(persistentData, ALWAYS_REQUIRE_PROPER_TOOL, alwaysRequireProperTool);

		if (miner) {
			mob.goalSelector.addGoal(1, new BlockBreakerGoal(mob, maxDistance, timeToBreakMultiplier1, toolOnly, properToolOnly, properToolRequired));
			if (equipStonePick && mob.getOffhandItem().isEmpty())
			{
				mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.STONE_PICKAXE));
				mob.setDropChance(EquipmentSlot.OFFHAND, -1f);
			}
		}
	}

	public static boolean isDimensionBlackOrNotWhitelisted(Level level) {
		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : dimensionBlacklist.blacklist) {
			if (blacklistEntry.location.equals(level.dimension().location())) {
				if (!dimensionBlacklist.blacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}

		return isInBlacklist || (!isInWhitelist && dimensionBlacklist.blacklistAsWhitelist);
	}
}
