package insane96mcp.enhancedai.modules.zombie.ai;

import insane96mcp.enhancedai.modules.zombie.feature.PearlerZombie;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class PearlUseGoal extends Goal {

	//Runs every other tick
	private final Zombie pearler;
	private Player targetPlayer;
	private int cooldown = reducedTickDelay(50);

	ThrownEnderpearl enderPearlEntity;

	public PearlUseGoal(Zombie pearler){
		this.pearler = pearler;
	}

	public boolean canUse() {
		LivingEntity target = this.pearler.getTarget();
		if (!(target instanceof Player))
			return false;

		//6 blocks distance
		if (this.pearler.distanceToSqr(target) < 36d)
			return false;

		if (--this.cooldown > 0)
			return false;

		return this.pearler.getMainHandItem().getItem() == Items.ENDER_PEARL || this.pearler.getOffhandItem().getItem() == Items.ENDER_PEARL;
	}

	public boolean canContinueToUse() {
		return this.enderPearlEntity != null && this.enderPearlEntity.isAlive();
	}

	public void start() {
		this.targetPlayer = (Player) this.pearler.getTarget();
		EquipmentSlot slot = this.pearler.getMainHandItem().getItem() == Items.ENDER_PEARL ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
		this.pearler.level.playSound(null, this.pearler.getX(), this.pearler.getY(), this.pearler.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.HOSTILE, 1F, 0.4F / (this.pearler.getRandom().nextFloat() * 0.4F + 0.8F));
		ItemStack stack = this.pearler.getItemBySlot(slot);
		this.enderPearlEntity = new ThrownEnderpearl(this.pearler.level, this.pearler);
		enderPearlEntity.setPos(this.pearler.getEyePosition(1f).x, this.pearler.getEyePosition(1f).y, this.pearler.getEyePosition(1f).z);
		enderPearlEntity.setItem(stack);
		Vec3 vector3d = this.pearler.getEyePosition(1f);
		double d0 = this.targetPlayer.getX() - vector3d.x;
		double d1 = this.targetPlayer.getEyePosition(1f).y - vector3d.y;
		double d2 = this.targetPlayer.getZ() - vector3d.z;
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		double pitch = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
		double yaw = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
		enderPearlEntity.shootFromRotation(this.pearler, (float) (pitch - 3f - d1), (float) (yaw), 0.0F, 1.5F, PearlerZombie.inaccuracy);
		this.pearler.level.addFreshEntity(enderPearlEntity);
		stack.shrink(1);
		this.cooldown = reducedTickDelay(100);
	}

	public void stop() {
		this.targetPlayer = null;
		this.enderPearlEntity = null;
		this.pearler.getNavigation().stop();
	}
}
