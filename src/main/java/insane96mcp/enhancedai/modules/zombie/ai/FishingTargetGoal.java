package insane96mcp.enhancedai.modules.zombie.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class FishingTargetGoal extends Goal {

	//Runs every other tick
	private final Zombie fisher;
	private Player targetPlayer;
	private int cooldown = 20;
	private int reel;

	FishingHook fishingHook;

	public FishingTargetGoal(Zombie fisher){
		this.fisher = fisher;
	}

	public boolean canUse() {
		LivingEntity target = this.fisher.getTarget();
		if (!(target instanceof Player))
			return false;

		if (this.fisher.distanceToSqr(target) > 32d * 32d)
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
		this.fishingHook = new FishingHook(EntityType.FISHING_BOBBER, this.fisher.level);
		this.fishingHook.setOwner(this.targetPlayer);
		fishingHook.setPos(this.fisher.getEyePosition(1f).x, this.fisher.getEyePosition(1f).y, this.fisher.getEyePosition(1f).z);
		Vec3 vector3d = this.fisher.getEyePosition(1f);
		double d0 = this.targetPlayer.getX() - vector3d.x;
		double d1 = this.targetPlayer.getEyePosition(1f).y - vector3d.y;
		double d2 = this.targetPlayer.getZ() - vector3d.z;
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		double pitch = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
		double yaw = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
		fishingHook.shootFromRotation(this.fisher, (float) (pitch - 3f - d1), (float) (yaw), 0.0F, 1.5F, 1);
		this.fisher.level.addFreshEntity(fishingHook);
		this.reel = 20;
	}

	public void tick() {
		if (--this.reel > 0)
			return;

		fishingHook.retrieve(this.fisher.getMainHandItem());
	}

	public void stop() {
		this.targetPlayer = null;
		this.fishingHook = null;
		this.fisher.getNavigation().stop();
		this.cooldown = 30;
	}
}
