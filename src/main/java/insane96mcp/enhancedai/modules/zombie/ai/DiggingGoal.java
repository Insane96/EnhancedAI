package insane96mcp.enhancedai.modules.zombie.ai;

import insane96mcp.enhancedai.modules.zombie.feature.DiggerZombie;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class DiggingGoal extends Goal {

	private final Zombie digger;
	private LivingEntity target;
	private final double reachDistance;
	private final double maxDistanceFromTarget;
	private final List<BlockPos> targetBlocks = new ArrayList<>();
	private int tickToBreak = 0;
	private int breakingTick = 0;
	private BlockState blockState = null;
	private int prevBreakProgress = 0;
	private final boolean toolOnly;
	private final boolean properToolOnly;

	private Vec3 lastPosition = null;
	private int lastPositionTickstamp = 0;

	private Path path = null;

	public DiggingGoal(Zombie digger, double maxDistanceFromTarget, boolean toolOnly, boolean properToolOnly){
		this.digger = digger;
		this.reachDistance = 4;
		this.maxDistanceFromTarget = maxDistanceFromTarget == 0 ? 64 * 64 : maxDistanceFromTarget * maxDistanceFromTarget;
		this.toolOnly = toolOnly;
		this.properToolOnly = properToolOnly;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}

	public boolean canUse() {
		if (this.toolOnly && !(this.digger.getOffhandItem().getItem() instanceof DiggerItem))
			return false;

		if (this.digger.getTarget() == null)
			return false;

		return this.isStuck()
				&& this.digger.distanceToSqr(digger.getTarget()) > 2d
				&& this.digger.distanceToSqr(digger.getTarget()) < maxDistanceFromTarget;
	}

	public boolean canContinueToUse() {
		if (this.properToolOnly && this.blockState != null && !this.canHarvestBlock())
			return false;

		if (this.target == null || !this.target.isAlive())
			return false;

		return !this.targetBlocks.isEmpty()
				&& this.targetBlocks.get(0).distSqr(this.digger.blockPosition()) < this.reachDistance * this.reachDistance
				&& this.digger.getNavigation().isDone()
				&& !this.digger.level.getBlockState(this.targetBlocks.get(0)).isAir()
				&& this.path != null && this.path.getDistToTarget() > 1.5d;
	}

	public void start() {
		this.target = this.digger.getTarget();
		if (this.target == null)
			return;
		fillTargetBlocks();
		if (!this.targetBlocks.isEmpty())
			initBlockBreak();
	}

	public void stop() {
		this.target = null;
		if (!this.targetBlocks.isEmpty()) {
			this.digger.level.destroyBlockProgress(this.digger.getId(), targetBlocks.get(0), -1);
			this.targetBlocks.clear();
		}
		this.tickToBreak = 0;
		this.breakingTick = 0;
		this.blockState = null;
		this.prevBreakProgress = 0;
		this.lastPosition = null;
		this.path = null;
	}

	public void tick() {
		if (this.targetBlocks.isEmpty())
			return;
		if (this.properToolOnly && this.blockState != null && !this.canHarvestBlock())
			return;
		this.breakingTick++;
		this.digger.getLookControl().setLookAt(this.targetBlocks.get(0).getX() + 0.5d, this.targetBlocks.get(0).getY() + 0.5d, this.targetBlocks.get(0).getZ() + 0.5d);
		if (this.prevBreakProgress != (int) ((this.breakingTick / (float) this.tickToBreak) * 10)) {
			this.prevBreakProgress = (int) ((this.breakingTick / (float) this.tickToBreak) * 10);
			this.digger.level.destroyBlockProgress(this.digger.getId(), targetBlocks.get(0), this.prevBreakProgress);
		}
		if (this.breakingTick % 6 == 0) {
			this.digger.swing(InteractionHand.MAIN_HAND);
		}
		if (this.breakingTick % 4 == 0) {
			SoundType soundType = this.blockState.getSoundType(this.digger.level, this.targetBlocks.get(0), this.digger);
			this.digger.level.playSound(null, this.targetBlocks.get(0), soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		}
		if (this.breakingTick >= this.tickToBreak) {
			this.digger.level.destroyBlock(targetBlocks.get(0), false, this.digger);
			this.digger.level.destroyBlockProgress(this.digger.getId(), targetBlocks.get(0), -1);
			this.targetBlocks.remove(0);
			if (!this.targetBlocks.isEmpty())
				initBlockBreak();
			else if (this.digger.distanceToSqr(this.target) > 2d && !this.digger.getSensing().hasLineOfSight(this.target))
				start();
		}
	}

	private void initBlockBreak() {
		this.blockState = this.digger.level.getBlockState(this.targetBlocks.get(0));
		this.tickToBreak = computeTickToBreak();
		this.breakingTick = 0;
		this.path = this.digger.getNavigation().createPath(this.target, 1);
	}

	private void fillTargetBlocks() {
		int mobHeight = Mth.ceil(this.digger.getBbHeight());
		for (int i = 0; i < mobHeight; i++) {
			BlockHitResult rayTraceResult = this.digger.level.clip(new ClipContext(this.digger.position().add(0, i + 0.5d, 0), this.target.getEyePosition(1f).add(0, i, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.digger));
			if (rayTraceResult.getType() == HitResult.Type.MISS)
				continue;
			if (this.targetBlocks.contains(rayTraceResult.getBlockPos()))
				continue;
			if (rayTraceResult.getBlockPos().getY() > DiggerZombie.maxYDig)
				continue;

			double distance = this.digger.distanceToSqr(rayTraceResult.getLocation());
			if (distance > this.reachDistance * this.reachDistance)
				continue;

			BlockState state = this.digger.level.getBlockState(rayTraceResult.getBlockPos());

			if (state.hasBlockEntity() || state.getDestroySpeed(this.digger.level, rayTraceResult.getBlockPos()) == -1)
				continue;

			if (DiggerZombie.blockBlacklist.isBlockBlackOrNotWhiteListed(state.getBlock()))
				continue;

			if (state.hasBlockEntity() && DiggerZombie.blacklistTileEntities)
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
		if (this.digger.getTarget() == null)
			return false;

		if (this.lastPosition == null || this.digger.distanceToSqr(this.lastPosition) > 2.25d) {
			this.lastPosition = this.digger.position();
			this.lastPositionTickstamp = this.digger.tickCount;
		}
		return this.digger.getNavigation().isDone() || this.digger.tickCount - this.lastPositionTickstamp >= 60;
	}

	// Copy-paste of vanilla code
	private int computeTickToBreak() {
		int canHarvestBlock = this.canHarvestBlock() ? 30 : 100;
		double diggingSpeed = this.getDigSpeed() / this.blockState.getDestroySpeed(this.digger.level, this.targetBlocks.get(0)) / canHarvestBlock;
		return Mth.ceil((1f / diggingSpeed) * DiggerZombie.miningSpeedMultiplier);
	}

	private float getDigSpeed() {
		float digSpeed = this.digger.getOffhandItem().getDestroySpeed(this.blockState);
		if (digSpeed > 1.0F) {
			int efficiencyLevel = EnchantmentHelper.getBlockEfficiency(this.digger);
			ItemStack itemstack = this.digger.getOffhandItem();
			if (efficiencyLevel > 0 && !itemstack.isEmpty()) {
				digSpeed += (float)(efficiencyLevel * efficiencyLevel + 1);
			}
		}

		if (MobEffectUtil.hasDigSpeed(this.digger)) {
			digSpeed *= 1.0F + (float)(MobEffectUtil.getDigSpeedAmplification(this.digger) + 1) * 0.2F;
		}

		if (this.digger.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			//noinspection ConstantConditions
			float miningFatigueAmplifier = switch (this.digger.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
				case 0 -> 0.3F;
				case 1 -> 0.09F;
				case 2 -> 0.0027F;
				default -> 8.1E-4F;
			};

			digSpeed *= miningFatigueAmplifier;
		}

		if (this.digger.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(this.digger)) {
			digSpeed /= 5.0F;
		}

		return digSpeed;
	}

	private boolean canHarvestBlock() {
		if (!this.blockState.requiresCorrectToolForDrops())
			return true;

		ItemStack stack = this.digger.getOffhandItem();
		if (stack.isEmpty())
			return false;

		return stack.isCorrectToolForDrops(this.blockState);
	}
}
