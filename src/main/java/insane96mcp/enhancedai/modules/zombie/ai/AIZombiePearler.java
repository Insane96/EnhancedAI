package insane96mcp.enhancedai.modules.zombie.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class AIZombiePearler extends Goal {

	private final ZombieEntity pearler;
	private PlayerEntity targetPlayer;
	private int cooldown = 40;

	EnderPearlEntity enderPearlEntity;

	public AIZombiePearler(ZombieEntity pearler){
		this.pearler = pearler;
	}

	public boolean canUse() {
		LivingEntity target = this.pearler.getTarget();
		if (!(target instanceof PlayerEntity))
			return false;

		if (this.pearler.distanceToSqr(target) < 6d * 6d)
			return false;

		if (--this.cooldown > 0)
			return false;

		if (!this.pearler.isOnGround())
			return false;

		return this.pearler.getMainHandItem().getItem() == Items.ENDER_PEARL || this.pearler.getOffhandItem().getItem() == Items.ENDER_PEARL;
	}

	public boolean canContinueToUse() {
		return this.enderPearlEntity != null && this.enderPearlEntity.isAlive();
	}

	public void start() {
		this.targetPlayer = (PlayerEntity) this.pearler.getTarget();
		EquipmentSlotType slot = this.pearler.getMainHandItem().getItem() == Items.ENDER_PEARL ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND;
		this.pearler.level.playSound(null, this.pearler.getX(), this.pearler.getY(), this.pearler.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 1F, 0.4F / (this.pearler.level.random.nextFloat() * 0.4F + 0.8F));
		ItemStack stack = this.pearler.getItemBySlot(slot);
		this.enderPearlEntity = new EnderPearlEntity(this.pearler.level, this.pearler);
		enderPearlEntity.setPos(this.pearler.getEyePosition(1f).x, this.pearler.getEyePosition(1f).y, this.pearler.getEyePosition(1f).z);
		enderPearlEntity.setItem(stack);
		Vector3d vector3d = this.pearler.getEyePosition(1f);
		double d0 = this.targetPlayer.getX() - vector3d.x;
		double d1 = this.targetPlayer.getEyePosition(1f).y - vector3d.y;
		double d2 = this.targetPlayer.getZ() - vector3d.z;
		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
		double pitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
		double yaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
		enderPearlEntity.shootFromRotation(this.pearler, (float) (pitch - 3f - d1), (float) (yaw), 0.0F, 1.5F, 5);
		this.pearler.level.addFreshEntity(enderPearlEntity);
		stack.shrink(1);
		this.cooldown = 60;
	}

	public void stop() {
		this.targetPlayer = null;
		this.enderPearlEntity = null;
		this.pearler.getNavigation().stop();
	}
}
