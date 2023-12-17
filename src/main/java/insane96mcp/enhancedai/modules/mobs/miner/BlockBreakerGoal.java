package insane96mcp.enhancedai.modules.mobs.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class BlockBreakerGoal extends Goal {

	private final Mob miner;
	private LivingEntity target;
	private final double reachDistance;
	private final double maxDistanceFromTarget;
	private final double timeToBreakMultiplier;
	private final List<BlockPos> targetBlocks = new ArrayList<>();
	private int tickToBreak = 0;
	private int breakingTick = 0;
	private BlockState blockState = null;
	private int prevBreakProgress = 0;
	private final boolean toolOnly;
	private final boolean properToolOnly;
	private final boolean properToolRequired;

	private Vec3 lastPosition = null;
	private int lastPositionTickstamp = 0;

	private Path path = null;

	public BlockBreakerGoal(Mob miner, double maxDistanceFromTarget, double timeToBreakMultiplier, boolean toolOnly, boolean properToolOnly, boolean properToolRequired){
		this.miner = miner;
		this.reachDistance = 4;
		this.maxDistanceFromTarget = maxDistanceFromTarget == 0 ? 64 * 64 : maxDistanceFromTarget * maxDistanceFromTarget;
		this.timeToBreakMultiplier = timeToBreakMultiplier;
		this.toolOnly = toolOnly;
		this.properToolOnly = properToolOnly;
		this.properToolRequired = properToolRequired;
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	public boolean canUse() {
		if (this.toolOnly && !(this.miner.getOffhandItem().getItem() instanceof DiggerItem))
			return false;

		if (this.miner.getTarget() == null)
			return false;

		return this.isStuck()
				&& (this.miner.distanceToSqr(miner.getTarget()) > 2d || !this.miner.hasLineOfSight(miner.getTarget()))
				&& this.miner.distanceToSqr(miner.getTarget()) < maxDistanceFromTarget;
	}

	public boolean canContinueToUse() {
		if (this.targetBlocks.isEmpty())
			return false;
		if (this.properToolOnly && this.blockState != null && !this.canBreakBlock())
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
			this.miner.setAggressive(true);
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
		if (this.properToolOnly && this.blockState != null && !this.canBreakBlock())
			return;
		BlockPos pos = this.targetBlocks.get(0);
		this.breakingTick++;
		this.miner.getLookControl().setLookAt(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d);
		if (this.prevBreakProgress != (int) ((this.breakingTick / (float) this.tickToBreak) * 10)) {
			this.prevBreakProgress = (int) ((this.breakingTick / (float) this.tickToBreak) * 10);
			this.miner.level().destroyBlockProgress(this.miner.getId(), pos, this.prevBreakProgress);
		}
		if (this.breakingTick % 6 == 0) {
			this.miner.swing(InteractionHand.MAIN_HAND);
		}
		if (this.breakingTick % 4 == 0) {
			SoundType soundType = this.blockState.getSoundType(this.miner.level(), pos, this.miner);
			this.miner.level().playSound(null, pos, soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
		}
		if (this.breakingTick >= this.tickToBreak && this.miner.level() instanceof ServerLevel level) {
			BlockEntity blockentity = this.blockState.hasBlockEntity() ? this.miner.level().getBlockEntity(pos) : null;
			LootParams.Builder lootparams$builder = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, this.miner.getOffhandItem()).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.miner);
			this.blockState.spawnAfterBreak(level, pos, this.miner.getOffhandItem(), true);
			this.blockState.getDrops(lootparams$builder).forEach((itemStack) -> level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, itemStack)));
			this.miner.level().destroyBlock(pos, false, this.miner);
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
					|| rayTraceResult.getBlockPos().getY() > MinerMobs.maxY)
                continue;

            double distance = this.miner.distanceToSqr(rayTraceResult.getLocation());
			if (distance > this.reachDistance * this.reachDistance)
				continue;

			BlockState state = this.miner.level().getBlockState(rayTraceResult.getBlockPos());

            if (state.hasBlockEntity()
					|| state.getDestroySpeed(this.miner.level(), rayTraceResult.getBlockPos()) == -1
					|| state.is(MinerMobs.BLOCK_BLACKLIST)
					|| state.hasBlockEntity() && MinerMobs.blacklistTileEntities)
                continue;

            this.targetBlocks.add(rayTraceResult.getBlockPos());
		}
		Collections.reverse(this.targetBlocks);
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}

	/**
	 * Returns true if the zombie has been stuck in the same spot (radius 1.5 blocks) for more than 3 seconds
	 */
	public boolean isStuck() {
		if (this.miner.getTarget() == null)
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
		double diggingSpeed = this.getDigSpeed() / this.blockState.getDestroySpeed(this.miner.level(), this.targetBlocks.get(0)) / canHarvestBlock;
		return Mth.ceil((1f / diggingSpeed) * this.timeToBreakMultiplier);
	}

	private float getDigSpeed() {
		float digSpeed = this.miner.getOffhandItem().getDestroySpeed(this.blockState);
		if (digSpeed > 1.0F) {
			int efficiencyLevel = EnchantmentHelper.getBlockEfficiency(this.miner);
			ItemStack itemstack = this.miner.getOffhandItem();
			if (efficiencyLevel > 0 && !itemstack.isEmpty()) {
				digSpeed += (float)(efficiencyLevel * efficiencyLevel + 1);
			}
		}

		if (MobEffectUtil.hasDigSpeed(this.miner)) {
			digSpeed *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this.miner) + 1) * 0.2F;
		}

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

		if (this.miner.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(this.miner)) {
			digSpeed /= 5.0F;
		}

		return digSpeed;
	}

	private boolean canBreakBlock() {
		if (!ForgeEventFactory.onEntityDestroyBlock(this.miner, this.targetBlocks.get(0), this.blockState))
			return false;
		if (!this.blockState.requiresCorrectToolForDrops() || !this.properToolRequired)
			return true;

		ItemStack stack = this.miner.getOffhandItem();
		if (stack.isEmpty())
			return false;

		return stack.isCorrectToolForDrops(this.blockState);
	}

	private boolean canHarvestBlock() {
		if (!this.blockState.requiresCorrectToolForDrops())
			return true;

		ItemStack stack = this.miner.getOffhandItem();
		if (stack.isEmpty())
			return false;

		return stack.isCorrectToolForDrops(this.blockState);
	}
}
