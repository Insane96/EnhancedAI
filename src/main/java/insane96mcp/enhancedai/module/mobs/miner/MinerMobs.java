package insane96mcp.enhancedai.module.mobs.miner;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.AliasedEnum;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.data.EAIDataEnum;
import insane96mcp.enhancedai.data.EAIDataList;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.List;
import java.util.Map;

@LoadFeature(module = EAIModules.MOBS, description = "Mobs can mine blocks to reach the target. Uses off-hand item to mine by default, can be changed with Data Keys. Only mobs in the entity type tag enhancedai:mobs/can_mine can spawn with the ability to mine and blocks in the tag enhancedai:miner_blacklist cannot be mined. This feature also adds the block reach attribute to all entities.")
public class MinerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BE_MINER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_mine"));
	public static final TagKey<Block> BLOCK_BLACKLIST = TagKey.create(Registries.BLOCK, EnhancedAI.location("miner_blacklist"));
	public static final TagKey<Block> HAS_CORRECT_TOOL = TagKey.create(Registries.BLOCK, EnhancedAI.location("miner/has_correct_tool"));

	@Config(min = 0d, max = 1d, description = "Chance for a mob in the entity type tag enhancedai:mobs/can_mine to spawn with the miner ability")
	public static Double minerChance = 0.07d;
	@Config(description = """
			Which tool the mob needs (in the off-hand by default) to mine blocks.
			NO_TOOL: Can mine any block, even with an empty hand.
			ANY_TOOL: Needs any tool (pickaxe, axe, shovel, hoe), then can mine any block (e.g. can mine stone with a shovel).
			HARVEST: Can mine any block that drops without a tool, but needs the correct tool for blocks that require it (e.g. can mine dirt, planks and glass with anything, stone only with a pickaxe).
			MATCHING_TOOL: Needs the tool that matches the block (e.g. dirt only with a shovel, planks only with an axe, stone only with a pickaxe). Blocks not in the block tag enhancedai:miner/has_correct_tool (e.g. glass, wool) can be mined with any tool.""")
	public static ToolRequirement toolRequirement = ToolRequirement.HARVEST;
	@Config(min = -512, max = 1024, description = "Mobs can mine from the bottom of the world to this Y level.")
	public static Integer maxY = 320;
	@Config(min = 0, max = 128, description = "The maximum distance from the target at which the Mobs can mine. Set to 0 to always mine.")
	public static Integer maxTargetDistance = 0;
	@Config(min = 0d, max = 128d, description = "Multiplier for the time a mob takes to break blocks. E.g. with this set to 2, mobs will take twice the time to mine a block.")
	public static Double timeToBreakMultiplier = 1.25d;
	@Config(description = "Dimensions where mobs can mine.")
	public static List<String> dimensionWhitelist = List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
	@Config(description = "If true, the block tag enhancedai:miner_blacklist will be treated as a whitelist instead of blacklist")
	public static Boolean blockBlacklistAsWhitelist = false;
	@Config(description = "Mobs with Miner AI will not be able to break tile entities")
	public static Boolean blacklistTileEntities = true;
	@Config(description = "Mobs with Miner AI will spawn with a Stone Pickaxe in the off-hand that never drops.")
	public static Boolean equipStonePick = true;

	public static EAIData<Boolean> MINER;
	public static EAIDataEnum<ToolRequirement> TOOL_REQUIREMENT;
	public static EAIData<Integer> MAX_Y;
	public static EAIData<Integer> MAX_TARGET_DISTANCE;
	public static EAIData<Double> TIME_TO_BREAK_MULTIPLIER;
	public static EAIDataList<String> DIMENSION_WHITELIST;
	public static EAIData<Boolean> OFFHAND;

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
		OFFHAND = EAIData.ofBool(this.createDataKey("offhand"));
	}

	public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, Attributes.BLOCK_INTERACTION_RANGE))
				event.add(entityType, Attributes.BLOCK_INTERACTION_RANGE);
			if (!event.has(entityType, Attributes.BLOCK_BREAK_SPEED))
				event.add(entityType, Attributes.BLOCK_BREAK_SPEED);
			if (!event.has(entityType, Attributes.MINING_EFFICIENCY))
				event.add(entityType, Attributes.MINING_EFFICIENCY);
			if (!event.has(entityType, Attributes.SUBMERGED_MINING_SPEED))
				event.add(entityType, Attributes.SUBMERGED_MINING_SPEED);
		}
	}

	//Low priority so other mods can set persistent data
	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
		 		|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
		 		|| !mob.getType().is(CAN_BE_MINER))
			return;

		boolean isMiner = mob.getRandom().nextDouble() < minerChance;
		MINER.applyIfAbsent(mob, isMiner);
		if (MINER.get(mob) && equipStonePick && mob.getOffhandItem().isEmpty())
		{
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.STONE_PICKAXE));
			mob.setDropChance(EquipmentSlot.OFFHAND, -1f);
		}
		TOOL_REQUIREMENT.applyIfAbsent(mob, toolRequirement);
		MAX_Y.applyIfAbsent(mob, maxY);
		MAX_TARGET_DISTANCE.applyIfAbsent(mob, maxTargetDistance);
		TIME_TO_BREAK_MULTIPLIER.applyIfAbsent(mob, timeToBreakMultiplier);
		DIMENSION_WHITELIST.applyIfAbsent(mob, dimensionWhitelist);
		OFFHAND.applyIfAbsent(mob, true);
	}

	public static boolean isValidDimension(Mob mob) {
		List<String> dimensionWhitelist = DIMENSION_WHITELIST.get(mob);
		for (String dimension : dimensionWhitelist) {
			if (dimension.equals(mob.level().dimension().location().toString()))
				return true;
		}
		return false;
	}

	public enum ToolRequirement implements AliasedEnum<ToolRequirement> {
		NO_TOOL,
		ANY_TOOL,
		HARVEST,
		MATCHING_TOOL;

		private static final Map<String, ToolRequirement> ALIASES = Map.of(
				"NONE", NO_TOOL,
				"CORRECT_TOOL_FOR_REQUIRED", HARVEST,
				"CORRECT_TOOL_FOR_ANY_BLOCK", MATCHING_TOOL
		);

		@Override
		public Map<String, ToolRequirement> aliases() {
			return ALIASES;
		}
	}
}
