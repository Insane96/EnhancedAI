package insane96mcp.enhancedai.module.mobs.fisher;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class FishingTargetGoal extends Goal {

	//Runs every other tick
	private final Mob fisher;
	private LivingEntity target;
	private int cooldown = 0;
	private int inventoryHookCooldown = 0;

	private int reel;
	private int fishingHookLifetime = 0;
	private boolean hasHookedEntity = false;

	EAIFishingHook fishingHook;

	public FishingTargetGoal(Mob fisher){
		this.fisher = fisher;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	public boolean canUse() {
		this.target = this.fisher.getTarget();
		if (this.target == null
                || this.target.isDeadOrDying())
			return false;

		if (!this.fisher.getMainHandItem().is(FisherMobs.FISHER_RODS) && !this.fisher.getOffhandItem().is(FisherMobs.FISHER_RODS))
			return false;

		if (this.fisher.isUnderWater())
			return false;

		double attackRange = FisherMobs.ATTACK_RANGE.get(this.fisher);
		if (this.fisher.distanceToSqr(this.target) < attackRange * attackRange)
			return false;
		return this.fisher.getSensing().hasLineOfSight(this.target);
    }

    private double getFishRangeSqr() {
        double range = FisherMobs.FISH_RANGE.get(this.fisher);
        range *= range;
        return range;
    }

	public void start() {
        if (this.fisher.distanceTo(this.target) <= getFishRangeSqr())
            this.fisher.getNavigation().stop();
    }

	public void tick() {
		this.fisher.getLookControl().setLookAt(this.target);
		double distance = this.fisher.distanceTo(this.target);
		double distanceY = this.target.getY() - this.fisher.getY();
		double dirX = this.target.getX() - this.fisher.getX();
		double dirZ = this.target.getZ() - this.fisher.getZ();
		double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
		if (this.fisher.distanceToSqr(this.target) > getFishRangeSqr()) {
			if (this.fisher.getNavigation().isDone()) {
				Path path = this.fisher.getNavigation().createPath(this.target, 1);
				if (path != null)
					this.fisher.getNavigation().moveTo(path, 1);
			}
		}
		else {
            this.fisher.getNavigation().stop();
        }
        if (--this.cooldown > 0)
            return;
        if (this.reel <= 0 && this.fishingHook == null) {
            this.fisher.setAggressive(true);
            this.fishingHook = new EAIFishingHook(this.fisher, this.fisher.level());
            this.fishingHook.setPos(this.fisher.getEyePosition(1f).x, this.fisher.getEyePosition(1f).y + 0.1d, this.fisher.getEyePosition(1f).z);
            double yPos = this.target.getY(0d);
            yPos += this.target.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
            double dirY = yPos - this.fishingHook.getY();
            this.fishingHook.shoot(dirX, dirY + distanceXZ * 0.17d, dirZ, 1.1f + ((float) distance / 32f) + (float) Math.max(distanceY / 48d, 0f), FisherMobs.INACCURACY.get(this.fisher));
            this.fisher.level().addFreshEntity(fishingHook);
            this.fisher.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.HOSTILE, 2F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
            this.reel = this.adjustedTickDelay(FisherMobs.REEL_IN_TICKS.get(this.fisher));
            this.fishingHookLifetime = this.adjustedTickDelay(FisherMobs.FORCE_REEL_IN.get(this.fisher));
        }

		if (this.fishingHook != null && (this.fishingHook.getHookedIn() != null || --this.fishingHookLifetime <= 0 || this.fishingHook.onGround())) {
			--this.reel;
			if (--this.reel <= 0) {
				if (this.fishingHook.getHookedIn() != null)
					this.hasHookedEntity = true;
				this.fishingHook.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.0F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
				boolean isInventoryHooked = --this.inventoryHookCooldown <= 0 && this.fisher.getRandom().nextDouble() < FisherMobs.HOOK_HANDS_CHANCE.get(this.fisher);
				this.fishingHook.retrieve(isInventoryHooked);
                this.fishingHook = null;
				if (isInventoryHooked)
					this.inventoryHookCooldown = 4;
				this.cooldown = this.adjustedTickDelay(FisherMobs.COOLDOWN.get(this.fisher));
				if (hasHookedEntity)
					this.cooldown *= 2;
			}
		}
	}

	public void stop() {
		this.target = null;
		if (this.fishingHook != null) {
			this.fishingHook.kill();
			this.fishingHook = null;
		}
		this.reel = 0;
		this.fisher.setAggressive(false);
		this.hasHookedEntity = false;
	}
}
