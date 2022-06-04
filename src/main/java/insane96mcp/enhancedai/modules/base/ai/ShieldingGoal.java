package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.UseAnim;

public class ShieldingGoal extends Goal {
	private final Mob mob;
	private final double moveSpeedAmp;

	public ShieldingGoal(Mob mob, double moveSpeedAmpIn) {
		this.mob = mob;
		this.moveSpeedAmp = moveSpeedAmpIn;
		//this.setFlags(EnumSet.of(Flag.LOOK));
	}

	public boolean canUse() {
		return this.mob.getTarget() != null && this.canShield() && this.mob.getTarget().distanceToSqr(this.mob) < 30d;
	}

	protected boolean canShield() {
		return this.mob.isHolding(stack -> stack.getItem().getUseAnimation(stack).equals(UseAnim.BLOCK));
	}

	public void start() {
		super.start();
		this.mob.startUsingItem(this.mob.getMainHandItem().getItem().getUseAnimation(this.mob.getMainHandItem()).equals(UseAnim.BLOCK) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
		this.mob.getLookControl().setLookAt(this.mob.getTarget());
	}

	public void stop() {
		super.stop();
		this.mob.stopUsingItem();
	}

	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
