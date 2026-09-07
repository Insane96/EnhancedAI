package insane96mcp.enhancedai.module.mobs.pearler;

import insane96mcp.enhancedai.utils.MCUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class PearlUseGoal extends Goal {

	//Runs every other tick
	private final Mob pearler;
	private LivingEntity target;
	private int cooldown = this.adjustedTickDelay(50);

	public PearlUseGoal(Mob pearler){
		this.pearler = pearler;
	}

	public boolean canUse() {
		LivingEntity target = this.pearler.getTarget();
		if (target == null)
			return false;

		if (this.pearler.isUnderWater())
			return false;

		if (!this.pearler.getSensing().hasLineOfSight(target))
			return false;

		//5 blocks distance
		if (this.pearler.distanceToSqr(target) < 25d)
			return false;

		if (--this.cooldown > 0)
			return false;

		return this.pearler.isHolding(Items.ENDER_PEARL);
	}

	public boolean canContinueToUse() {
		return false;
	}

	public void start() {
		this.target = this.pearler.getTarget();
		EquipmentSlot slot = MCUtils.getEquipmentSlot(this.pearler, Items.ENDER_PEARL);
		if (slot == null)
			return;
		this.pearler.level().playSound(null, this.pearler.getX(), this.pearler.getY(), this.pearler.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.HOSTILE, 1F, 0.4F / (this.pearler.getRandom().nextFloat() * 0.4F + 0.8F));
		ItemStack stack = this.pearler.getItemBySlot(slot);
		ThrownEnderpearl thrownEnderPearl = new ThrownEnderpearl(this.pearler.level(), this.pearler);
		thrownEnderPearl.setPos(this.pearler.getEyePosition(1f).x, this.pearler.getEyePosition(1f).y, this.pearler.getEyePosition(1f).z);
		thrownEnderPearl.setItem(stack);
		Vec3 vector3d = this.pearler.getEyePosition(1f);
		double d0 = this.target.getX() - vector3d.x;
		double d1 = this.target.getEyePosition(1f).y - vector3d.y;
		double d2 = this.target.getZ() - vector3d.z;
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		double pitch = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
		double yaw = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
		thrownEnderPearl.shootFromRotation(this.pearler, (float) (pitch - 3f - d1), (float) (yaw), 0.0F, 1.5F, PearlerMobs.INACCURACY.get(this.pearler));
		this.pearler.level().addFreshEntity(thrownEnderPearl);
		stack.shrink(1);
		this.cooldown = this.adjustedTickDelay(100);
	}

	public void stop() {
		this.target = null;
	}
}
