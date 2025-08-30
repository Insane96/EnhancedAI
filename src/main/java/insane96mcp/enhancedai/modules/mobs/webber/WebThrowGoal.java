package insane96mcp.enhancedai.modules.mobs.webber;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class WebThrowGoal extends Goal {

	private final Mob mob;
	private LivingEntity target;

	private int cooldown;

	public WebThrowGoal(Mob mob){
		this.mob = mob;
	}

	public boolean canUse() {
		this.target = this.mob.getTarget();
		if (this.target == null)
			return false;

		double distance = this.mob.distanceToSqr(this.target);
		if (--this.cooldown > 0)
			return false;

		double minDistance = ThrowingWeb.DISTANCE_MIN.get(this.mob);
		minDistance *= minDistance;
		double maxDistance = ThrowingWeb.DISTANCE_MAX.get(this.mob);
		maxDistance *= maxDistance;
		return distance > minDistance && distance < maxDistance;
	}

	public boolean canContinueToUse() {
		return false;
	}

	public void start() {
		if (!this.mob.hasLineOfSight(this.target))
			return;
		double distance = this.mob.distanceTo(this.target);
		double distanceY = this.target.getY() - this.mob.getY();
		float f = 2.0F / 3.0F;
		ThrownWebEntity thrownWeb = new ThrownWebEntity(this.mob.level(), this.mob);
		double d0 = this.target.getX() - this.mob.getX();
		double d2 = this.target.getZ() - this.mob.getZ();
		double distanceXZ = Math.sqrt(d0 * d0 + d2 * d2);
		double yPos = this.target.getY(0d);
		yPos += this.target.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
		double d1 = yPos - thrownWeb.getY();
		thrownWeb.shoot(d0, d1 + distanceXZ * 0.18d, d2, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 0);
		thrownWeb.setDamage(ThrowingWeb.DAMAGE.get(this.mob).floatValue());
		thrownWeb.level().addFreshEntity(thrownWeb);
		this.mob.playSound(SoundEvents.SPIDER_HURT, 1.0F, 2.0F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
		this.cooldown = adjustedTickDelay(ThrowingWeb.COOLDOWN.get(this.mob));
	}

	public void onHit() {
		this.cooldown += adjustedTickDelay(100);
	}

	public void stop() {
		this.target = null;
	}
}
