package insane96mcp.enhancedai.modules.zombie.ai;

import insane96mcp.enhancedai.modules.Modules;
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

	//TODO Add a few ticks before checking for no path to prevent instant mining as soon as the player moves
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

	public AIZombieDigger(ZombieEntity digger, boolean toolOnly, boolean properToolOnly){
		this.digger = digger;
		this.reachDistance = 4;
		this.toolOnly = toolOnly;
		this.properToolOnly = properToolOnly;
		this.setMutexFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	public boolean shouldExecute() {
		if (this.toolOnly && !(this.digger.getHeldItemOffhand().getItem() instanceof ToolItem))
			return false;

		LivingEntity target = digger.getAttackTarget();
		if (!(target instanceof PlayerEntity))
			return false;

		return (this.digger.getNavigator().noPath() || this.digger.getNavigator().func_244428_t())
				&& this.digger.getDistanceSq(target) > 1.5d;
	}

	public boolean shouldContinueExecuting() {
		if (this.properToolOnly && this.blockState != null && !this.canHarvestBlock())
			return false;

		return !this.targetBlocks.isEmpty()
				&& this.targetPlayer != null
				&& this.targetPlayer.isAlive()
				&& this.targetBlocks.get(0).distanceSq(this.digger.getPosition()) < this.reachDistance * this.reachDistance
				&& this.digger.getNavigator().noPath()
				&& !this.digger.world.getBlockState(this.targetBlocks.get(0)).isAir();
	}

	public void startExecuting() {
		this.targetPlayer = (PlayerEntity) this.digger.getAttackTarget();
		fillTargetBlocks();
		if (!this.targetBlocks.isEmpty())
			initBlockBreak();
	}

	public void resetTask() {
		this.targetPlayer = null;
		if (!this.targetBlocks.isEmpty()) {
			this.digger.world.sendBlockBreakProgress(this.digger.getEntityId(), targetBlocks.get(0), -1);
			this.targetBlocks.clear();
		}
		this.tickToBreak = 0;
		this.breakingTick = 0;
		this.blockState = null;
		this.prevBreakProgress = 0;
	}

	public void tick() {
		if (this.targetBlocks.isEmpty())
			return;
		if (this.properToolOnly && this.blockState != null && !this.canHarvestBlock())
			return;
		this.breakingTick++;
		this.digger.getLookController().setLookPosition(this.targetBlocks.get(0).getX() + 0.5d, this.targetBlocks.get(0).getY() + 0.5d, this.targetBlocks.get(0).getZ() + 0.5d);
		if (this.prevBreakProgress != (int) ((this.breakingTick / (float) this.tickToBreak) * 10)) {
			this.digger.world.sendBlockBreakProgress(this.digger.getEntityId(), targetBlocks.get(0), this.prevBreakProgress);
			this.prevBreakProgress = (int) ((this.breakingTick / (float) this.tickToBreak) * 10);
		}
		if (this.breakingTick % 6 == 0) {
			this.digger.swingArm(Hand.MAIN_HAND);
		}
		if (this.breakingTick % 4 == 0) {
			SoundType soundType = this.blockState.getSoundType(this.digger.world, this.targetBlocks.get(0), this.digger);
			this.digger.world.playSound(null, this.targetBlocks.get(0), soundType.getHitSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
		}
		if (this.breakingTick >= this.tickToBreak) {
			this.digger.world.destroyBlock(targetBlocks.get(0), true, this.digger);
			this.digger.world.sendBlockBreakProgress(this.digger.getEntityId(), targetBlocks.get(0), -1);
			this.targetBlocks.remove(0);
			if (!this.targetBlocks.isEmpty())
				initBlockBreak();
		}
	}

	private void initBlockBreak() {
		this.blockState = this.digger.world.getBlockState(this.targetBlocks.get(0));
		this.tickToBreak = computeTickToBreak();
		this.breakingTick = 0;
	}

	private int computeTickToBreak() {
		int canHarvestBlock = this.canHarvestBlock() ? 30 : 100;
		double diggingSpeed = this.getDigSpeed() / this.blockState.getBlockHardness(this.digger.world, this.targetBlocks.get(0)) / canHarvestBlock;
		return MathHelper.ceil((1f / diggingSpeed) * Modules.zombie.diggerZombie.miningSpeedMultiplier);
	}

	private float getDigSpeed() {
		float digSpeed = this.digger.getHeldItemOffhand().getDestroySpeed(this.blockState);
		if (digSpeed > 1.0F) {
			int efficiencyLevel = EnchantmentHelper.getEfficiencyModifier(this.digger);
			ItemStack itemstack = this.digger.getHeldItemOffhand();
			if (efficiencyLevel > 0 && !itemstack.isEmpty()) {
				digSpeed += (float)(efficiencyLevel * efficiencyLevel + 1);
			}
		}

		if (EffectUtils.hasMiningSpeedup(this.digger)) {
			digSpeed *= 1.0F + (float)(EffectUtils.getMiningSpeedup(this.digger) + 1) * 0.2F;
		}

		if (this.digger.isPotionActive(Effects.MINING_FATIGUE)) {
			float miningFatigueAmplifier;
			switch(this.digger.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) {
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

		if (this.digger.areEyesInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this.digger)) {
			digSpeed /= 5.0F;
		}

		if (!this.digger.isOnGround()) {
			digSpeed /= 5.0F;
		}

		return digSpeed;
	}

	private boolean canHarvestBlock()
	{
		if (!this.blockState.getRequiresTool())
			return true;

		ItemStack stack = this.digger.getHeldItemOffhand();
		ToolType tool = this.blockState.getHarvestTool();
		if (stack.isEmpty() || tool == null)
			return false;

		int toolLevel = stack.getHarvestLevel(tool, null, this.blockState);
		if (toolLevel < 0)
			return false;

		return toolLevel >= this.blockState.getHarvestLevel();
	}

	private void fillTargetBlocks() {
		int mobHeight = MathHelper.ceil(this.digger.getHeight());
		for (int i = 0; i < mobHeight; i++) {
			BlockRayTraceResult rayTraceResult = this.digger.world.rayTraceBlocks(new RayTraceContext(this.digger.getPositionVec().add(0, i, 0), this.targetPlayer.getEyePosition(1f).add(0, i, 0), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this.digger));
			if (rayTraceResult.getType() == RayTraceResult.Type.MISS)
				continue;
			if (this.targetBlocks.contains(rayTraceResult.getPos()))
				continue;
			double distance = this.digger.getDistanceSq(rayTraceResult.getHitVec());
			if (distance > this.reachDistance * this.reachDistance)
				continue;

			this.targetBlocks.add(rayTraceResult.getPos());
		}
		Collections.reverse(this.targetBlocks);
	}
}
