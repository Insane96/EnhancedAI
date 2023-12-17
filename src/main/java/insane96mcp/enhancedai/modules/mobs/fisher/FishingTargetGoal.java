package insane96mcp.enhancedai.modules.mobs.fisher;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Items;

public class FishingTargetGoal extends Goal {

	//Runs every other tick
	private final Mob fisher;
	private LivingEntity target;
	private int cooldown = reducedTickDelay(60);

	private int reel;
	private int fishingHookLifetime = 0;

	FishingHook fishingHook;

	public FishingTargetGoal(Mob fisher){
		this.fisher = fisher;
	}

	public boolean canUse() {
		LivingEntity target = this.fisher.getTarget();
		if (target == null)
			return false;

		if (this.fisher.isInWaterOrBubble())
			return false;

		//24 & 1 blocks
		if (this.fisher.distanceToSqr(target) > 576d
				|| this.fisher.distanceToSqr(target) <= 1d
				|| !this.fisher.getSensing().hasLineOfSight(target))
			return false;

		if (--this.cooldown > 0)
			return false;

		return this.fisher.getMainHandItem().getItem() == Items.FISHING_ROD || this.fisher.getOffhandItem().getItem() == Items.FISHING_ROD;
	}

	public boolean canContinueToUse() {
		return this.fishingHook != null && this.fishingHook.isAlive();
	}

	public void start() {
		this.target = this.fisher.getTarget();
		this.fisher.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.HOSTILE, 1F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
		this.fishingHook = new FishingHook(this.fisher, this.fisher.level());
		this.fishingHook.setPos(this.fisher.getEyePosition(1f).x, this.fisher.getEyePosition(1f).y + 0.1d, this.fisher.getEyePosition(1f).z);
		double distance = this.fisher.distanceTo(this.target);
		double distanceY = this.target.getY() - this.fisher.getY();
		double dirX = this.target.getX() - this.fisher.getX();
		double dirZ = this.target.getZ() - this.fisher.getZ();
		double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
		double yPos = this.target.getY(0d);
		yPos += this.target.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
		double dirY = yPos - this.fishingHook.getY();
		this.fishingHook.shoot(dirX, dirY + distanceXZ * 0.17d, dirZ, 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 1);
		this.fisher.level().addFreshEntity(fishingHook);
		this.reel = reducedTickDelay((int) FisherMobs.reelInTicks.getByDifficulty(this.fisher.level()));
		this.fishingHookLifetime = reducedTickDelay(60);
	}

	public void tick() {
		this.fisher.getLookControl().setLookAt(this.target);
		boolean isOnGround = this.fishingHook.onGround();
		if (isOnGround || this.fishingHook.getHookedIn() != null || --this.fishingHookLifetime <= 0) {
			if (isOnGround)
				--this.reel;
			if (--this.reel <= 0) {
				this.fishingHook.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.0F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
				this.fishingHook.retrieve(this.fisher.getRandom().nextDouble() < FisherMobs.hookInventoryChance);
			}
		}
	}

	public void stop() {
		this.target = null;
		this.fishingHook = null;
		this.cooldown = reducedTickDelay(60);
	}
}
