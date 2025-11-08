package insane96mcp.enhancedai.modules.mobs.miner;

import insane96mcp.enhancedai.modules.mobs.MeleeAttacking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.mobs.miner.persistence.BlockRespawnData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static insane96mcp.enhancedai.modules.mobs.miner.MinerMobs.blacklistTileEntities;

public class MineTowardsTargetGoal extends Goal {
	private final Mob miner;
	private LivingEntity target;
	private final double reachDistance;
	private final List<BlockPos> targetBlocks = new ArrayList<>();
	private int tickToBreak = 0;
	private int breakingTick = 0;
	private BlockState blockState = null;
	private int prevBreakProgress = 0;

	private Vec3 lastPosition = null;
	private int lastPositionTickstamp = 0;

	private Path path = null;

	public MineTowardsTargetGoal(Mob miner) {
		this.miner = miner;
		this.reachDistance = miner.getAttribute(ForgeMod.BLOCK_REACH.get()) == null ? 4.5 : miner.getAttributeValue(ForgeMod.BLOCK_REACH.get());
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}



	public boolean canUse() {
		if (!MinerMobs.isValidDimension(this.miner)
				|| !this.miner.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
				|| (MinerMobs.TOOL_REQUIREMENT.get(this.miner) == MinerMobs.ToolRequirement.ANY_TOOL && !(this.miner.getOffhandItem().getItem() instanceof DiggerItem))
				|| this.miner.getTarget() == null)
			return false;
		float maxTargetDistance = MinerMobs.MAX_TARGET_DISTANCE.get(this.miner);
		maxTargetDistance *= maxTargetDistance;
		return this.isStuck()
				&& (!MeleeAttacking.isWithinMeleeAttackRange(this.miner, this.miner.getTarget()) || !this.miner.getSensing().hasLineOfSight(this.miner.getTarget()))
				&& (this.miner.distanceToSqr(miner.getTarget()) < maxTargetDistance || maxTargetDistance == 0);
	}


	public boolean canContinueToUse() {
		if (this.targetBlocks.isEmpty())
			return false;
		if (this.blockState != null && !this.canBreakBlock())
			return false;
		if (this.target == null || !this.target.isAlive())
			return false;

		return this.targetBlocks.get(0).distSqr(this.miner.blockPosition()) < this.reachDistance * this.reachDistance
				&& this.miner.getNavigation().isDone()
				&& !this.miner.level().getBlockState(this.targetBlocks.get(0)).isAir()
				&& this.path != null && (this.path.getDistToTarget() > 1.5d || !this.miner.hasLineOfSight(this.target));
	}


	public void start() {
		this.target = this.miner.getTarget();
		if (this.target == null)
			return;
		fillTargetBlocks();
		if (!this.targetBlocks.isEmpty()) {
			initBlockBreak();
		}
	}


	public void stop() {
		this.target = null;
		if (!this.targetBlocks.isEmpty()) {
			this.miner.level().destroyBlockProgress(this.miner.getId(), targetBlocks.get(0), -1);
			this.targetBlocks.clear();
		}
		this.tickToBreak = 0;
		this.breakingTick = 0;
		this.blockState = null;
		this.prevBreakProgress = 0;
		this.lastPosition = null;
		this.path = null;
		this.miner.setAggressive(false);
	}


	public void tick() {
		if (this.targetBlocks.isEmpty())
			return;
		if (this.blockState != null && !this.canBreakBlock())
			return;
		this.miner.setAggressive(true);
		BlockPos pos = this.targetBlocks.get(0);
		this.breakingTick++;
		this.miner.getLookControl().setLookAt(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);

		// progress visuals
		int progress = (int) ((this.breakingTick / (float) this.tickToBreak) * 10);
		if (this.prevBreakProgress != progress) {
			this.prevBreakProgress = progress;
			this.miner.level().destroyBlockProgress(this.miner.getId(), pos, progress);
		}

		if (this.breakingTick % 6 == 0)
			this.miner.swing(InteractionHand.MAIN_HAND);

		if (this.breakingTick % 4 == 0) {
			SoundType soundType = this.blockState.getSoundType(this.miner.level(), pos, this.miner);
			this.miner.level().playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS,
					(soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
		}

		// --- Respawn-aware block breaking ---
		if (this.breakingTick >= this.tickToBreak && this.miner.level() instanceof ServerLevel level) {
			if (!ForgeEventFactory.onEntityDestroyBlock(this.miner, pos, this.blockState)) return;

			int respawnTime = MinerMobs.BLOCK_RESPAWN_TIME.get(this.miner);
			boolean scaleByHardness = MinerMobs.SCALE_RESPAWN_BY_HARDNESS.get(this.miner);

			if (scaleByHardness) {
				double hardness = Math.max(0, this.blockState.getDestroySpeed(level, pos));
				int baseTime = MinerMobs.BASE_RESPAWN_TIME.get(this.miner);
				double multiplier = MinerMobs.HARDNESS_RESPAWN_MULTIPLIER.get(this.miner);
				respawnTime = (int) Math.ceil(baseTime + hardness * multiplier);
			}

			boolean willRespawn = respawnTime > 0;

			CompoundTag blockNbt = null;
			if (willRespawn && MinerMobs.shouldSaveBlockNBT(this.blockState)) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity != null)
					blockNbt = blockEntity.saveWithFullMetadata();
			}

			if (willRespawn) {
				// Remove block without drops, record for respawn
				level.removeBlockEntity(pos);
				level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);

				long respawnAt = level.getGameTime() + respawnTime;
				BlockRespawnData data = BlockRespawnData.get(level);
				data.set(pos, respawnAt, this.blockState, blockNbt);

				EnhancedAI.LOGGER.debug("Scheduled respawn for block {} at {} after {} ticks",
						this.blockState.getBlock().getName().getString(), pos, respawnTime);
			}
			else {
				// Normal destruction with drops
				if (this.miner.getItemBySlot(EquipmentSlot.OFFHAND).isCorrectToolForDrops(this.blockState)) {
					BlockEntity blockEntity = this.blockState.hasBlockEntity() ? level.getBlockEntity(pos) : null;
					LootParams.Builder lootparams = (new LootParams.Builder(level))
							.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
							.withParameter(LootContextParams.TOOL, this.miner.getOffhandItem())
							.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
							.withOptionalParameter(LootContextParams.THIS_ENTITY, this.miner);
					this.blockState.spawnAfterBreak(level, pos, this.miner.getOffhandItem(), false);
					this.blockState.getDrops(lootparams).forEach(stack ->
							level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack)));
					level.removeBlock(pos, false);
				}
			}

			this.miner.level().destroyBlockProgress(this.miner.getId(), pos, -1);
			this.targetBlocks.remove(0);
			if (!this.targetBlocks.isEmpty())
				initBlockBreak();
			else if (this.miner.distanceToSqr(this.target) > 2d && !this.miner.getSensing().hasLineOfSight(this.target))
				start();
		}
	}

	private void initBlockBreak() {
		this.blockState = this.miner.level().getBlockState(this.targetBlocks.get(0));
		this.tickToBreak = computeTickToBreak();
		this.breakingTick = 0;
		this.path = this.miner.getNavigation().createPath(this.target, 1);
	}

	private void fillTargetBlocks() {
		int mobHeight = Mth.ceil(this.miner.getBbHeight());
		for (int i = 0; i < mobHeight; i++) {
			BlockHitResult rayTrace = this.miner.level().clip(new ClipContext(this.miner.position().add(0, i + 0.5d, 0), this.target.getEyePosition(1f).add(0, i, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.miner));
			if (rayTrace.getType() == HitResult.Type.MISS
					|| this.targetBlocks.contains(rayTrace.getBlockPos())
					|| rayTrace.getBlockPos().getY() > MinerMobs.MAX_Y.get(this.miner))
				continue;

			double distance = this.miner.distanceToSqr(rayTrace.getLocation());
			if (distance > this.reachDistance * this.reachDistance)
				continue;

			BlockState state = this.miner.level().getBlockState(rayTrace.getBlockPos());

			if (state.getDestroySpeed(this.miner.level(), rayTrace.getBlockPos()) == -1
					|| (state.hasBlockEntity() && blacklistTileEntities))
				continue;

			boolean listed = state.is(MinerMobs.BLOCK_BLACKLIST);
			if (listed != MinerMobs.blockBlacklistAsWhitelist)
				continue;

			this.targetBlocks.add(rayTrace.getBlockPos());
		}
		Collections.reverse(this.targetBlocks);
	}

	private int computeTickToBreak() {
		int canHarvestBlock = this.canHarvestBlock() ? 30 : 100;
		double diggingSpeed = this.getDigSpeed() / this.blockState.getDestroySpeed(this.miner.level(), this.targetBlocks.get(0)) / canHarvestBlock;
		return Mth.ceil((1f / diggingSpeed) * MinerMobs.TIME_TO_BREAK_MULTIPLIER.get(this.miner));
	}

	private float getDigSpeed() {
		float digSpeed = this.miner.getOffhandItem().getDestroySpeed(this.blockState);
		if (digSpeed > 1.0F) {
			int efficiency = EnchantmentHelper.getBlockEfficiency(this.miner);
			if (efficiency > 0) digSpeed += (float) (efficiency * efficiency + 1);
		}
		if (MobEffectUtil.hasDigSpeed(this.miner))
			digSpeed *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(this.miner) + 1) * 0.2F;
		if (this.miner.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float f = switch (this.miner.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
				case 0 -> 0.3F;
				case 1 -> 0.09F;
				case 2 -> 0.0027F;
				default -> 8.1E-4F;
			};
			digSpeed *= f;
		}
		if (this.miner.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(this.miner))
			digSpeed /= 5.0F;
		return digSpeed;
	}

	private boolean canBreakBlock() {
		MinerMobs.ToolRequirement toolReq = MinerMobs.TOOL_REQUIREMENT.get(this.miner);
		if (toolReq == MinerMobs.ToolRequirement.NONE || toolReq == MinerMobs.ToolRequirement.ANY_TOOL)
			return true;
		if (toolReq == MinerMobs.ToolRequirement.CORRECT_TOOL_FOR_REQUIRED && !this.blockState.requiresCorrectToolForDrops())
			return true;
		ItemStack stack = this.miner.getOffhandItem();
		return !stack.isEmpty() && stack.isCorrectToolForDrops(this.blockState);
	}

	private boolean canHarvestBlock() {
		if (!this.blockState.requiresCorrectToolForDrops())
			return true;
		ItemStack stack = this.miner.getOffhandItem();
		return !stack.isEmpty() && stack.isCorrectToolForDrops(this.blockState);
	}

	public boolean requiresUpdateEveryTick() { return true; }

	private boolean isStuck() {
		if (this.miner.getTarget() == null)
			return false;
		if (MeleeAttacking.isWithinMeleeAttackRange(this.miner, this.miner.getTarget())
				&& this.miner.getSensing().hasLineOfSight(this.miner.getTarget()))
			return false;
		if (this.lastPosition == null || this.miner.distanceToSqr(this.lastPosition) > 2.25d) {
			this.lastPosition = this.miner.position();
			this.lastPositionTickstamp = this.miner.tickCount;
		}
		return this.miner.getNavigation().isDone() || this.miner.tickCount - this.lastPositionTickstamp >= 60;
	}

	private void freeSpaceForRespawn(ServerLevel level, BlockPos pos) {
		BlockState existing = level.getBlockState(pos);

		// If block is already air, nothing to do
		if (existing.isAir()) return;

		// Drop the block as an item
		existing.spawnAfterBreak(level, pos, ItemStack.EMPTY, true);
		level.removeBlock(pos, false);

		// Move any entities standing on the block slightly upward
		level.getEntities(null, existing.getShape(level, pos).bounds().move(pos.getX(), pos.getY(), pos.getZ()))
				.forEach(e -> e.setPos(e.getX(), e.getY() + 1.0, e.getZ()));
	}
}
