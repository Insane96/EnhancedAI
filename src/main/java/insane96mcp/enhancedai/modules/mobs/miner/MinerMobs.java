package insane96mcp.enhancedai.modules.mobs.miner;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.data.EAIDataEnum;
import insane96mcp.enhancedai.data.EAIDataList;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.miner.persistence.BlockRespawnData;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.level.block.Block;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@LoadFeature(module = Modules.Ids.MOBS, description = "Mobs can mine blocks to reach the target. Uses offhand item to mine. Only mobs in the entity type tag enhancedai:mobs/can_mine can spawn with the ability to mine and blocks in the tag enhancedai:miner_blacklist cannot be mined. This feature also adds the block reach attribute to all entities.")
public class MinerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BE_MINER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_mine"));
	public static final TagKey<Block> BLOCK_BLACKLIST = TagKey.create(Registries.BLOCK, EnhancedAI.location("miner_blacklist"));

	@Config(min = 0d, max = 1d, description = "Chance for a mob in the entity type tag enhancedai:can_be_miner to spawn with the miner ability")
	public static Double minerChance = 0.07d;
	@Config(description = "NONE: No item is required. ANY_TOOL: Any item that can break blocks in the off-hand is required. CORRECT_TOOL_FOR_REQUIRED: The mob is able to mine any blocks that don't require a tool, and require a tool for blocks that require it (e.g. can always mine dirt but can't mine stone). CORRECT_TOOL_FOR_ANY_BLOCK: The mob can only mine blocks if the tool is the right one for the block (e.g. can mine dirt only with a shovel).")
	public static ToolRequirement toolRequirement = ToolRequirement.CORRECT_TOOL_FOR_REQUIRED;
	@Config(min = -512, max = 1024, description = "Mobs can mine from the bottom of the world to this Y level.")
	public static Integer maxY = 320;
	@Config(min = 0, max = 128, description = "The maximum distance from the target at which the Mobs can mine. Set to 0 to always mine.")
	public static Integer maxTargetDistance = 0;
	@Config(min = 0d, max = 128d, description = "Multiplier for the time a mob takes to break blocks. E.g. with this set to 2, mobs will take twice the time to mine a block.")
	public static Double timeToBreakMultiplier = 1.25d;
	@Config(description = "Dimensions where mobs can mine.")
	public static List<String> dimensionWhitelist = List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
	@Config(description = "If true, the block tag `enhancedai:miner_mobs/blacklist` will be treated as a whitelist instead of blacklist")
	public static Boolean blockBlacklistAsWhitelist = false;
	@Config(description = "Mobs with Miner AI will not be able to break tile entities")
	public static Boolean blacklistTileEntities = true;
	@Config(description = "Mobs with Miner AI will spawn with a Stone Pickaxe that never drops.")
	public static Boolean equipStonePick = true;
	@Config(min = 0, max = 1200, description = "Time in ticks for a mined block to respawn. Set to 0 for no respawn. (20 ticks = 1 second)")
	public static Integer blockRespawnTime = 0;
	@Config(description = "If true, block respawn time will scale based on the block's hardness.")
	public static Boolean scaleRespawnByHardness = false;
	@Config(min = 0, max = 1200, description = "Base respawn time in ticks for a block (used if scaling by hardness is enabled).")
	public static Integer baseRespawnTime = 200;
	@Config(min = 0d, max = 100d, description = "Multiplier applied to the block's hardness when calculating respawn time (used if scaling by hardness is enabled).")
	public static Double hardnessRespawnMultiplier = 100d;

	public static EAIData<Boolean> MINER;
	public static EAIDataEnum<ToolRequirement> TOOL_REQUIREMENT;
	public static EAIData<Integer> MAX_Y;
	public static EAIData<Integer> MAX_TARGET_DISTANCE;
	public static EAIData<Double> TIME_TO_BREAK_MULTIPLIER;
	public static EAIDataList<String> DIMENSION_WHITELIST;
	public static EAIData<Integer> BLOCK_RESPAWN_TIME;
	public static EAIData<Boolean> SCALE_RESPAWN_BY_HARDNESS;
	public static EAIData<Integer> BASE_RESPAWN_TIME;
	public static EAIData<Double> HARDNESS_RESPAWN_MULTIPLIER;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		MINER = EAIData.ofBool(this.createDataKey("miner"), (mob, miner) -> {
			GoalHelper.removeGoal(mob.goalSelector, MineTowardsTargetGoal.class);
			if (miner)
				mob.goalSelector.addGoal(1, new MineTowardsTargetGoal(mob));
		});
		TOOL_REQUIREMENT = EAIDataEnum.of(this.createDataKey("tool_requirement"), ToolRequirement.class);
		MAX_Y = EAIData.ofInt(this.createDataKey("max_y"));
		MAX_TARGET_DISTANCE = EAIData.ofInt(this.createDataKey("max_target_distance"));
		TIME_TO_BREAK_MULTIPLIER = EAIData.ofDouble(this.createDataKey("time_to_break_multiplier"));
		DIMENSION_WHITELIST = EAIDataList.of(this.createDataKey("dimension_whitelist"), String.class);
		BLOCK_RESPAWN_TIME = EAIData.ofInt(this.createDataKey("block_respawn_time"));
		SCALE_RESPAWN_BY_HARDNESS = EAIData.ofBool(this.createDataKey("scale_respawn_by_hardness"));
		BASE_RESPAWN_TIME = EAIData.ofInt(this.createDataKey("base_respawn_time"));
		HARDNESS_RESPAWN_MULTIPLIER = EAIData.ofDouble(this.createDataKey("hardness_respawn_multiplier"));
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(MineTowardsTargetGoal.class);
	}

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (event.has(entityType, ForgeMod.BLOCK_REACH.get()))
				continue;

			event.add(entityType, ForgeMod.BLOCK_REACH.get());
		}
	}

	//Lowest priority so other mods can set persistent data
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_BE_MINER))
			return;

		boolean isMiner = mob.getRandom().nextDouble() < minerChance;
		MINER.applyIfAbsent(mob, isMiner);
		if (isMiner && equipStonePick && mob.getOffhandItem().isEmpty())
		{
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.STONE_PICKAXE));
			mob.setDropChance(EquipmentSlot.OFFHAND, -1f);
		}
		TOOL_REQUIREMENT.applyIfAbsent(mob, toolRequirement);
		MAX_Y.applyIfAbsent(mob, maxY);
		MAX_TARGET_DISTANCE.applyIfAbsent(mob, maxTargetDistance);
		TIME_TO_BREAK_MULTIPLIER.applyIfAbsent(mob, timeToBreakMultiplier);
		DIMENSION_WHITELIST.applyIfAbsent(mob, dimensionWhitelist);
		BLOCK_RESPAWN_TIME.applyIfAbsent(mob, blockRespawnTime);
		SCALE_RESPAWN_BY_HARDNESS.applyIfAbsent(mob, scaleRespawnByHardness);
		BASE_RESPAWN_TIME.applyIfAbsent(mob, baseRespawnTime);
		HARDNESS_RESPAWN_MULTIPLIER.applyIfAbsent(mob, hardnessRespawnMultiplier);

	}

	public static boolean isValidDimension(Mob mob) {
		List<String> dimensionWhitelist = DIMENSION_WHITELIST.get(mob);
		for (String dimension : dimensionWhitelist) {
			if (dimension.equals(mob.level().dimension().location().toString()))
				return true;
		}
		return false;
	}

	public enum ToolRequirement {
		NONE,
		ANY_TOOL,
		CORRECT_TOOL_FOR_REQUIRED,
		CORRECT_TOOL_FOR_ANY_BLOCK
	}
	public static boolean shouldSaveBlockNBT(BlockState state) {
		if (state.hasBlockEntity()) return true;
		// Could add a tag whitelist or any other dynamic condition
		return false;
	}
	// --- Merged BlockRespawnHandler logic ---
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		MinecraftServer server = event.getServer();
		if (server == null)
			return;

		for (ServerLevel level : server.getAllLevels()) {
			BlockRespawnData data = BlockRespawnData.get(level);
			long time = level.getGameTime();
			boolean changed = false;

			Iterator<Map.Entry<BlockPos, BlockRespawnData.RespawnEntry>> it = data.getEntries().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<BlockPos, BlockRespawnData.RespawnEntry> entry = it.next();
				BlockPos pos = entry.getKey();
				BlockRespawnData.RespawnEntry info = entry.getValue();

				if (time >= info.time) {
					try {
						BlockState existing = level.getBlockState(pos);
						if (!existing.isAir()) {
							BlockEntity existingBe = level.getBlockEntity(pos);

							LootParams.Builder lootBuilder = new LootParams.Builder(level)
									.withParameter(LootContextParams.ORIGIN, pos.getCenter())
									.withOptionalParameter(LootContextParams.BLOCK_ENTITY, existingBe)
									.withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY);

							for (ItemStack drop : existing.getDrops(lootBuilder)) {
								level.addFreshEntity(new ItemEntity(level,
										pos.getX() + 0.5d,
										pos.getY() + 0.5d,
										pos.getZ() + 0.5d,
										drop));
							}

							existing.spawnAfterBreak(level, pos, ItemStack.EMPTY, false);
							level.removeBlock(pos, false);
						}

						AABB box = new AABB(pos);
						for (Entity e : level.getEntities(null, box)) {
							e.setPos(e.getX(), e.getY() + 1.0, e.getZ());
						}

						level.setBlock(pos, info.state, 3);

						if (info.nbt != null) {
							BlockEntity be = level.getBlockEntity(pos);
							if (be != null) {
								be.load(info.nbt);
								be.setChanged();
							}
							else {
								EnhancedAI.LOGGER.warn("BlockEntity missing at {} when respawning; NBT skipped.", pos);
							}
						}

						EnhancedAI.LOGGER.debug("Respawned block {} at {}", info.state.getBlock().getName().getString(), pos);

					}
					catch (Exception e) {
						EnhancedAI.LOGGER.warn("Failed to respawn block at {}: {}", pos, e.getMessage());
					}

					it.remove();
					changed = true;
				}
			}

			if (changed)
				data.setDirty();
		}
	}
}
