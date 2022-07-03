package insane96mcp.enhancedai.modules.zombie.ai;

import insane96mcp.enhancedai.modules.zombie.entity.projectile.FishingHook;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class FishingTargetGoal extends Goal {

	//Runs every other tick
	private final Zombie fisher;
	private Player targetPlayer;
	private int cooldown = 20;

	private int reel;
	private int fishingHookLifetime = 0;

	FishingHook fishingHook;

	public FishingTargetGoal(Zombie fisher){
		this.fisher = fisher;
	}

	public boolean canUse() {
		LivingEntity target = this.fisher.getTarget();
		if (!(target instanceof Player))
			return false;

		//24d & 4d
		if (this.fisher.distanceToSqr(target) > 576d
				|| this.fisher.distanceToSqr(target) < 16d
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
		this.targetPlayer = (Player) this.fisher.getTarget();
		this.fisher.level.playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.HOSTILE, 1F, 0.4F / (this.fisher.level.random.nextFloat() * 0.4F + 0.8F));
		this.fishingHook = new FishingHook(this.fisher, this.fisher.level);
		this.fishingHook.setPos(this.fisher.getEyePosition(1f).x, this.fisher.getEyePosition(1f).y, this.fisher.getEyePosition(1f).z);
		Vec3 vector3d = this.fisher.getEyePosition(1f);
		double distance = this.fisher.distanceTo(this.targetPlayer);
		double distanceY = this.targetPlayer.getY() - this.fisher.getY();
		double dirX = this.targetPlayer.getX() - this.fisher.getX();
		double dirZ = this.targetPlayer.getZ() - this.fisher.getZ();
		double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
		double yPos = this.targetPlayer.getY(0d);
		yPos += this.targetPlayer.getEyeHeight() * 0.5 + (distanceY / distanceXZ);
		double dirY = yPos - this.fishingHook.getY();
		this.fishingHook.shoot(dirX, dirY + distanceXZ * 0.17d, dirZ, 1.1f + ((float)distance / 32f) + (float)Math.max(distanceY / 48d, 0f), 1);
		this.fisher.level.addFreshEntity(fishingHook);
		this.reel = reducedTickDelay(40);
		this.fishingHookLifetime = reducedTickDelay(40);
	}

	public void tick() {
		if (this.fishingHook.isOnGround() || this.fishingHook.getHookedIn() != null || --this.fishingHookLifetime <= 0) {
			if (--this.reel <= 0) {
				this.fishingHook.level.playSound((Player)null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.0F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
				this.fishingHook.retrieve();
			}
		}
	}

	public void stop() {
		this.targetPlayer = null;
		this.fishingHook = null;
		this.cooldown = reducedTickDelay(40);
	}
}
