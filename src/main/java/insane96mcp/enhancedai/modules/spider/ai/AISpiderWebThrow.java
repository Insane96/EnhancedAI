package insane96mcp.enhancedai.modules.spider.ai;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.spider.entity.projectile.ThrownWebEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class AISpiderWebThrow extends Goal {

	private final SpiderEntity spider;
	private PlayerEntity targetPlayer;

	private int cooldown = Modules.spider.throwingWeb.throwingCooldown;

	public AISpiderWebThrow(SpiderEntity spider){
		this.spider = spider;
	}

	public boolean canUse() {
		LivingEntity target = this.spider.getTarget();
		if (!(target instanceof PlayerEntity))
			return false;

		if (this.spider.distanceToSqr(target) < 2d * 2d)
			return false;

		return --this.cooldown <= 0;
	}

	public boolean canContinueToUse() {
		return false;
	}

	public void start() {
		this.targetPlayer = (PlayerEntity) this.spider.getTarget();
		if (!this.spider.canSee(this.targetPlayer))
			return;
		double distance = this.spider.distanceTo(this.targetPlayer);
		double distanceY = this.targetPlayer.getY() - this.spider.getY();
		float f = 2.0F / 3.0F;
		ThrownWebEntity thrownWeb = new ThrownWebEntity(this.spider.level, this.spider);
		double d0 = this.targetPlayer.getX() - this.spider.getX();
		double d2 = this.targetPlayer.getZ() - this.spider.getZ();
		double distanceXZ = MathHelper.sqrt(d0 * d0 + d2 * d2);
		double yPos = this.targetPlayer.getY(0d);
		yPos += this.targetPlayer.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
		double d1 = yPos - thrownWeb.getY();
		thrownWeb.shoot(d0, d1 + distanceXZ * 0.2d, d2, f * 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 0);
		thrownWeb.setDamage((float) Modules.spider.throwingWeb.thrownWebDamage);
		thrownWeb.level.addFreshEntity(thrownWeb);
		this.spider.playSound(SoundEvents.SPIDER_HURT, 1.0F, 2.0F / (this.spider.getRandom().nextFloat() * 0.4F + 0.8F));
		this.cooldown = Modules.spider.throwingWeb.throwingCooldown;
	}

	public void stop() {
		this.targetPlayer = null;
	}
}
