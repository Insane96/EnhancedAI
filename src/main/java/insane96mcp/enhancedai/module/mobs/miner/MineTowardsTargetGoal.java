package insane96mcp.enhancedai.module.mobs.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

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

	public MineTowardsTargetGoal(Mob miner){
		this.miner = miner;
		this.reachDistance = miner.getAttribute(Attributes.BLOCK_INTERACTION_RANGE) == null ? 4.5 : miner.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	public boolean canUse() {
		if (!MinerMobs.isValidDimension(this.miner)
				|| !this.miner.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
				|| (MinerMobs.TOOL_REQUIREMENT.get(this.miner) == MinerMobs.ToolRequirement.ANY_TOOL && !(this.getToolStack().getItem() instanceof DiggerItem))
				|| this.miner.getTarget() == null)
			return false;
		float maxTargetDistance = MinerMobs.MAX_TARGET_DISTANCE.get(this.miner);
		maxTargetDistance *= maxTargetDistance;
		return this.isStuck()
				&& (!this.miner.isWithinMeleeAttackRange(this.miner.getTarget()) || !this.miner.getSensing().hasLineOfSight(this.miner.getTarget()))
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
		if (this.prevBreakProgress != (int) ((this.breakingTick / (float) this.tickToBreak) * 10)) {
			this.prevBreakProgress = (int) ((this.breakingTick / (float) this.tickToBreak) * 10);
			this.miner.level().destroyBlockProgress(this.miner.getId(), pos, this.prevBreakProgress);
		}
		if (this.breakingTick % 6 == 0) {
			this.miner.swing(this.getToolHand());
		}
		if (this.breakingTick % 4 == 0) {
			SoundType soundType = this.blockState.getSoundType(this.miner.level(), pos, this.miner);
			this.miner.level().playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
		}
		if (this.breakingTick >= this.tickToBreak && this.miner.level() instanceof ServerLevel level) {
			if (EventHooks.onEntityDestroyBlock(this.miner, this.targetBlocks.get(0), this.blockState) && this.miner.level().destroyBlock(pos, false, this.miner) && (!this.blockState.requiresCorrectToolForDrops() || this.getToolStack().isCorrectToolForDrops(this.blockState))) {
				BlockEntity blockentity = this.blockState.hasBlockEntity() ? this.miner.level().getBlockEntity(pos) : null;
				LootParams.Builder lootparams$builder = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, this.getToolStack()).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.miner);
				this.blockState.spawnAfterBreak(level, pos, this.getToolStack(), false);
				this.blockState.getDrops(lootparams$builder).forEach((itemStack) -> level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, itemStack)));
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
			BlockHitResult rayTraceResult = this.miner.level().clip(new ClipContext(this.miner.position().add(0, i + 0.5d, 0), this.target.getEyePosition(1f).add(0, i, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.miner));
            if (rayTraceResult.getType() == HitResult.Type.MISS
					|| this.targetBlocks.contains(rayTraceResult.getBlockPos())
					|| rayTraceResult.getBlockPos().getY() > MinerMobs.MAX_Y.get(this.miner))
                continue;

            double distance = this.miner.distanceToSqr(rayTraceResult.getLocation());
			if (distance > this.reachDistance * this.reachDistance)
				continue;

			BlockState state = this.miner.level().getBlockState(rayTraceResult.getBlockPos());

			if (state.getDestroySpeed(this.miner.level(), rayTraceResult.getBlockPos()) == -1
					|| (state.hasBlockEntity() && MinerMobs.blacklistTileEntities))
				continue;

			boolean listed = state.is(MinerMobs.BLOCK_BLACKLIST);
			if (listed != MinerMobs.blockBlacklistAsWhitelist)
				continue;

            this.targetBlocks.add(rayTraceResult.getBlockPos());
		}
		Collections.reverse(this.targetBlocks);
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}

	/**
	 * Returns true if the miner has been stuck in the same spot (radius 1.5 blocks) for more than 3 seconds
	 */
	public boolean isStuck() {
		if (this.miner.getTarget() == null)
			return false;

		if (this.miner.isWithinMeleeAttackRange(this.miner.getTarget()) && this.miner.getSensing().hasLineOfSight(this.miner.getTarget()))
			return false;
		if (this.lastPosition == null || this.miner.distanceToSqr(this.lastPosition) > 2.25d) {
			this.lastPosition = this.miner.position();
			this.lastPositionTickstamp = this.miner.tickCount;
		}
		return this.miner.getNavigation().isDone() || this.miner.tickCount - this.lastPositionTickstamp >= 60;
	}

	// Copy-paste of vanilla code
	private int computeTickToBreak() {
		int canHarvestBlock = this.canHarvestBlock() ? 30 : 100;
		//TODO Maybe check dig speed each tick
		double diggingSpeed = this.getDigSpeed() / this.blockState.getDestroySpeed(this.miner.level(), this.targetBlocks.get(0)) / canHarvestBlock;
		return Mth.ceil((1f / diggingSpeed) * MinerMobs.TIME_TO_BREAK_MULTIPLIER.get(this.miner));
	}

	private float getDigSpeed() {
		float digSpeed = this.getToolStack().getDestroySpeed(this.blockState);
		if (digSpeed > 1.0F) {
			digSpeed += (float) this.miner.getAttributeValue(Attributes.MINING_EFFICIENCY);
		}

		if (MobEffectUtil.hasDigSpeed(this.miner))
			digSpeed *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this.miner) + 1) * 0.2F;

		if (this.miner.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			//noinspection ConstantConditions
			float miningFatigueAmplifier = switch (this.miner.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
				case 0 -> 0.3F;
				case 1 -> 0.09F;
				case 2 -> 0.0027F;
				default -> 8.1E-4F;
			};

			digSpeed *= miningFatigueAmplifier;
		}

		digSpeed *= (float)this.miner.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
		if (this.miner.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()))
			digSpeed *= (float) this.miner.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();

		return digSpeed;
	}

	private boolean canBreakBlock() {
		MinerMobs.ToolRequirement toolRequirement = MinerMobs.TOOL_REQUIREMENT.get(this.miner);
		if (toolRequirement == MinerMobs.ToolRequirement.NONE || toolRequirement == MinerMobs.ToolRequirement.ANY_TOOL)
			return true;
		if ((toolRequirement == MinerMobs.ToolRequirement.CORRECT_TOOL_FOR_REQUIRED) && !this.blockState.requiresCorrectToolForDrops())
			return true;

		ItemStack stack = this.getToolStack();
		if (stack.isEmpty())
			return false;

		return stack.isCorrectToolForDrops(this.blockState);
	}

	private boolean canHarvestBlock() {
		if (!this.blockState.requiresCorrectToolForDrops())
			return true;

		ItemStack stack = this.getToolStack();
		if (stack.isEmpty())
			return false;

		return stack.isCorrectToolForDrops(this.blockState);
	}

	private EquipmentSlot getToolSlot() {
		return MinerMobs.OFFHAND.get(this.miner) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
	}

	private InteractionHand getToolHand() {
		return MinerMobs.OFFHAND.get(this.miner) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	private ItemStack getToolStack() {
		return this.miner.getItemBySlot(this.getToolSlot());
	}
}
