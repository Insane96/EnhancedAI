package insane96mcp.enhancedai.modules.zombie.ai;

import net.minecraft.command.arguments.EntityAnchorArgument;
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

public class AIZombiePearler extends Goal {

	private final ZombieEntity pearler;
	private PlayerEntity targetPlayer;

	public AIZombiePearler(ZombieEntity pearler){
		this.pearler = pearler;
	}

	public boolean shouldExecute() {
		LivingEntity target = this.pearler.getAttackTarget();
		if (!(target instanceof PlayerEntity))
			return false;

		if (this.pearler.getDistanceSq(target) < 16d * 16d)
			return false;

		return this.pearler.getHeldItemMainhand().getItem() == Items.ENDER_PEARL || this.pearler.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
	}

	public boolean shouldContinueExecuting() {
		return false;
	}

	public void startExecuting() {
		this.targetPlayer = (PlayerEntity) this.pearler.getAttackTarget();
		EquipmentSlotType slot = this.pearler.getHeldItemMainhand().getItem() == Items.ENDER_PEARL ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND;
		this.pearler.world.playSound(null, this.pearler.getPosX(), this.pearler.getPosY(), this.pearler.getPosZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (this.pearler.world.rand.nextFloat() * 0.4F + 0.8F));
		ItemStack stack = this.pearler.getItemStackFromSlot(slot);
		EnderPearlEntity enderpearlentity = new EnderPearlEntity(this.pearler.world, this.pearler);
		enderpearlentity.setItem(stack);
		this.pearler.lookAt(EntityAnchorArgument.Type.EYES, this.targetPlayer.getEyePosition(1f));
		double d0 = this.targetPlayer.getPosX() - this.pearler.getPosX();
		double d1 = this.targetPlayer.getPosYHeight(0.3333333333333333D) - this.pearler.getPosYHeight(0.3333333333333333D);
		double d2 = this.targetPlayer.getPosZ() - this.pearler.getPosZ();
		double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
		enderpearlentity.setDirectionAndMovement(this.pearler, this.pearler.rotationPitch, this.pearler.rotationYaw, 0.0F, 1.5F, 0);
		this.pearler.world.addEntity(enderpearlentity);
		stack.shrink(1);
	}

	public void resetTask() {
		this.targetPlayer = null;
	}
}
