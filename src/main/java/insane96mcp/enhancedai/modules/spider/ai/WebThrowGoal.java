package insane96mcp.enhancedai.modules.spider.ai;

import insane96mcp.enhancedai.modules.spider.entity.projectile.ThrownWebEntity;
import insane96mcp.enhancedai.modules.spider.feature.ThrowingWeb;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;

public class WebThrowGoal extends Goal {

	private final Spider spider;
	private Player targetPlayer;

	private int cooldown = adjustedTickDelay((int) ThrowingWeb.throwingCooldown.min);

	public WebThrowGoal(Spider spider){
		this.spider = spider;
	}

	public boolean canUse() {
		LivingEntity target = this.spider.getTarget();
		if (!(target instanceof Player))
			return false;

		double distance = this.spider.distanceToSqr(target);

		if (distance < ThrowingWeb.distance.min * ThrowingWeb.distance.min || distance > ThrowingWeb.distance.max * ThrowingWeb.distance.max)
			return false;

		return --this.cooldown <= 0;
	}

	public boolean canContinueToUse() {
		return false;
	}

	public void start() {
		this.targetPlayer = (Player) this.spider.getTarget();
		if (this.targetPlayer == null)
			return;
		if (!this.spider.hasLineOfSight(this.targetPlayer))
			return;
		double distance = this.spider.distanceTo(this.targetPlayer);
		double distanceY = this.targetPlayer.getY() - this.spider.getY();
		float f = 2.0F / 3.0F;
		ThrownWebEntity thrownWeb = new ThrownWebEntity(this.spider.level, this.spider);
		double d0 = this.targetPlayer.getX() - this.spider.getX();
		double d2 = this.targetPlayer.getZ() - this.spider.getZ();
		double distanceXZ = Math.sqrt(d0 * d0 + d2 * d2);
		double yPos = this.targetPlayer.getY(0d);
		yPos += this.targetPlayer.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
		double d1 = yPos - thrownWeb.getY();
		thrownWeb.shoot(d0, d1 + distanceXZ * 0.18d, d2, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 0);
		thrownWeb.setDamage(ThrowingWeb.thrownWebDamage.floatValue());
		thrownWeb.level.addFreshEntity(thrownWeb);
		this.spider.playSound(SoundEvents.SPIDER_HURT, 1.0F, 2.0F / (this.spider.getRandom().nextFloat() * 0.4F + 0.8F));
		this.cooldown = adjustedTickDelay(ThrowingWeb.throwingCooldown.getIntRandBetween(spider.getRandom()));
	}

	public void stop() {
		this.targetPlayer = null;
	}
}
