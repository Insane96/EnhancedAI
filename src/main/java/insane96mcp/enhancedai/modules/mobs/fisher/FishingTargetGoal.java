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
	private int cooldown = 0;
	private int inventoryHookCooldown = 0;

	private int reel;
	private int fishingHookLifetime = 0;
	private boolean hasHookedEntity = false;

	FishingHook fishingHook;

	public FishingTargetGoal(Mob fisher){
		this.fisher = fisher;
	}

	public boolean canUse() {
		LivingEntity target = this.fisher.getTarget();
		if (target == null)
			return false;

		if (this.fisher.getMainHandItem().getItem() != Items.FISHING_ROD && this.fisher.getOffhandItem().getItem() != Items.FISHING_ROD)
			return false;

		if (--this.cooldown > 0)
			return false;

		if (this.fisher.isUnderWater())
			return false;

		//24 & 1 blocks
        return this.fisher.distanceToSqr(target) <= 576d
                && this.fisher.distanceToSqr(target) > 2d
                && this.fisher.getSensing().hasLineOfSight(target);
    }

	public boolean canContinueToUse() {
		return this.fishingHook != null && this.fishingHook.isAlive();
	}

	public void start() {
		this.target = this.fisher.getTarget();
		this.fisher.setAggressive(true);
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
		if (this.fishingHook.getHookedIn() != null || --this.fishingHookLifetime <= 0) {
			--this.reel;
			if (--this.reel <= 0) {
				if (this.fishingHook.getHookedIn() != null)
					this.hasHookedEntity = true;
				this.fishingHook.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.0F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
				boolean isInventoryHooked = --this.inventoryHookCooldown <= 0 && this.fisher.getRandom().nextDouble() < FisherMobs.hookHandsChance;
				this.fishingHook.retrieve(isInventoryHooked);
				if (isInventoryHooked)
					this.inventoryHookCooldown = 4;
			}
		}
	}

	public void stop() {
		this.target = null;
		this.fishingHook = null;
		this.cooldown = reducedTickDelay((int) FisherMobs.cooldown.getByDifficulty(this.fisher.level()));
		if (hasHookedEntity)
			this.cooldown *= 2;
		this.fisher.setAggressive(false);
		this.hasHookedEntity = false;
	}
}
