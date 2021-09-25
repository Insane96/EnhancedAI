package insane96mcp.enhancedai.modules.zombie.ai;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.utils.IdTagMatcher;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraftforge.common.ToolType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class AIZombieDigger extends Goal {

	private final ZombieEntity digger;
	private PlayerEntity targetPlayer;
	private final double reachDistance;
	private final List<BlockPos> targetBlocks = new ArrayList<>();
	private int tickToBreak = 0;
	private int breakingTick = 0;
	private BlockState blockState = null;
	private int prevBreakProgress = 0;
	private final boolean toolOnly;
	private final boolean properToolOnly;

	private int ticksWithNoPath = 0;

	public AIZombieDigger(ZombieEntity digger, boolean toolOnly, boolean properToolOnly){
		this.digger = digger;
		this.reachDistance = 4;
		this.toolOnly = toolOnly;
		this.properToolOnly = properToolOnly;
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	public boolean canUse() {
		if (this.toolOnly && !(this.digger.getOffhandItem().getItem() instanceof ToolItem))
			return false;

		LivingEntity target = digger.getTarget();
		if (!(target instanceof PlayerEntity))
			return false;

		if (this.digger.getNavigation().isDone() || this.digger.getNavigation().isStuck())
			this.ticksWithNoPath++;
		else if (this.ticksWithNoPath > 0)
			this.ticksWithNoPath--;

		return ticksWithNoPath >= 30
				&& this.digger.distanceToSqr(target) > 1.5d;
	}

	public boolean canContinueToUse() {
		if (this.properToolOnly && this.blockState != null && !this.canHarvestBlock())
			return false;

		return !this.targetBlocks.isEmpty()
				&& this.targetPlayer != null
				&& this.targetPlayer.isAlive()
				&& this.targetBlocks.get(0).distSqr(this.digger.blockPosition()) < this.reachDistance * this.reachDistance
				&& this.digger.getNavigation().isDone()
				&& !this.digger.level.getBlockState(this.targetBlocks.get(0)).isAir();
	}

	public void start() {
		this.targetPlayer = (PlayerEntity) this.digger.getTarget();
		fillTargetBlocks();
		if (!this.targetBlocks.isEmpty())
			initBlockBreak();
	}

	public void stop() {
		this.targetPlayer = null;
		if (!this.targetBlocks.isEmpty()) {
			this.digger.level.destroyBlockProgress(this.digger.getId(), targetBlocks.get(0), -1);
			this.targetBlocks.clear();
		}
		this.tickToBreak = 0;
		this.breakingTick = 0;
		this.blockState = null;
		this.prevBreakProgress = 0;
		this.ticksWithNoPath = 0;
	}

	public void tick() {
		if (this.targetBlocks.isEmpty())
			return;
		if (this.properToolOnly && this.blockState != null && !this.canHarvestBlock())
			return;
		this.breakingTick++;
		this.digger.getLookControl().setLookAt(this.targetBlocks.get(0).getX() + 0.5d, this.targetBlocks.get(0).getY() + 0.5d, this.targetBlocks.get(0).getZ() + 0.5d);
		if (this.prevBreakProgress != (int) ((this.breakingTick / (float) this.tickToBreak) * 10)) {
			this.digger.level.destroyBlockProgress(this.digger.getId(), targetBlocks.get(0), this.prevBreakProgress);
			this.prevBreakProgress = (int) ((this.breakingTick / (float) this.tickToBreak) * 10);
		}
		if (this.breakingTick % 6 == 0) {
			this.digger.swing(Hand.MAIN_HAND);
		}
		if (this.breakingTick % 4 == 0) {
			SoundType soundType = this.blockState.getSoundType(this.digger.level, this.targetBlocks.get(0), this.digger);
			this.digger.level.playSound(null, this.targetBlocks.get(0), soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		}
		if (this.breakingTick >= this.tickToBreak) {
			this.digger.level.destroyBlock(targetBlocks.get(0), true, this.digger);
			this.digger.level.destroyBlockProgress(this.digger.getId(), targetBlocks.get(0), -1);
			this.targetBlocks.remove(0);
			if (!this.targetBlocks.isEmpty())
				initBlockBreak();
		}
	}

	private void initBlockBreak() {
		this.blockState = this.digger.level.getBlockState(this.targetBlocks.get(0));
		this.tickToBreak = computeTickToBreak();
		this.breakingTick = 0;
	}

	private int computeTickToBreak() {
		int canHarvestBlock = this.canHarvestBlock() ? 30 : 100;
		double diggingSpeed = this.getDigSpeed() / this.blockState.getDestroySpeed(this.digger.level, this.targetBlocks.get(0)) / canHarvestBlock;
		return MathHelper.ceil((1f / diggingSpeed) * Modules.zombie.diggerZombie.miningSpeedMultiplier);
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

		if (EffectUtils.hasDigSpeed(this.digger)) {
			digSpeed *= 1.0F + (float)(EffectUtils.getDigSpeedAmplification(this.digger) + 1) * 0.2F;
		}

		if (this.digger.hasEffect(Effects.DIG_SLOWDOWN)) {
			float miningFatigueAmplifier;
			switch (this.digger.getEffect(Effects.DIG_SLOWDOWN).getAmplifier()) {
				case 0:
					miningFatigueAmplifier = 0.3F;
					break;
				case 1:
					miningFatigueAmplifier = 0.09F;
					break;
				case 2:
					miningFatigueAmplifier = 0.0027F;
					break;
				case 3:
				default:
					miningFatigueAmplifier = 8.1E-4F;
			}

			digSpeed *= miningFatigueAmplifier;
		}

		if (this.digger.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this.digger)) {
			digSpeed /= 5.0F;
		}

		if (!this.digger.isOnGround()) {
			digSpeed /= 5.0F;
		}

		return digSpeed;
	}

	private boolean canHarvestBlock() {
		if (!this.blockState.requiresCorrectToolForDrops())
			return true;

		ItemStack stack = this.digger.getOffhandItem();
		ToolType tool = this.blockState.getHarvestTool();
		if (stack.isEmpty() || tool == null)
			return false;

		int toolLevel = stack.getHarvestLevel(tool, null, this.blockState);
		if (toolLevel < 0)
			return false;

		return toolLevel >= this.blockState.getHarvestLevel();
	}

	private void fillTargetBlocks() {
		int mobHeight = MathHelper.ceil(this.digger.getBbHeight());
		for (int i = 0; i < mobHeight; i++) {
			BlockRayTraceResult rayTraceResult = this.digger.level.clip(new RayTraceContext(this.digger.position().add(0, i, 0), this.targetPlayer.getEyePosition(1f).add(0, i, 0), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.digger));
			if (rayTraceResult.getType() == RayTraceResult.Type.MISS)
				continue;
			if (this.targetBlocks.contains(rayTraceResult.getBlockPos()))
				continue;
			double distance = this.digger.distanceToSqr(rayTraceResult.getLocation());
			if (distance > this.reachDistance * this.reachDistance)
				continue;

			BlockState state = this.digger.level.getBlockState(rayTraceResult.getBlockPos());

			if (state.hasTileEntity())
				continue;

			//Check for black/whitelist
			boolean isInWhitelist = false;
			boolean isInBlacklist = false;
			for (IdTagMatcher blacklistEntry : Modules.zombie.diggerZombie.blockBlacklist) {
				if (!Modules.zombie.diggerZombie.blockBlacklistAsWhitelist) {
					if (blacklistEntry.matchesBlock(state.getBlock())) {
						isInBlacklist = true;
						break;
					}
				}
				else {
					if (blacklistEntry.matchesBlock(state.getBlock())) {
						isInWhitelist = true;
						break;
					}
				}
			}
			if (isInBlacklist || (!isInWhitelist && Modules.zombie.diggerZombie.blockBlacklistAsWhitelist))
				continue;

			this.targetBlocks.add(rayTraceResult.getBlockPos());
		}
		Collections.reverse(this.targetBlocks);
	}
}
