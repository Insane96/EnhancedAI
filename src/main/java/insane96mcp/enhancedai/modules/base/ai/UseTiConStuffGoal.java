package insane96mcp.enhancedai.modules.base.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.item.SlimeskullItem;

import javax.annotation.Nullable;

public class UseTiConStuffGoal extends Goal {
    private final Mob mob;

    public UseTiConStuffGoal(Mob mob) {
        this.mob = mob;
        //this.setFlags(EnumSet.of(Flag.LOOK));
    }

    public boolean canUse() {
        ItemStack headStack = this.getHelmet();
        if (headStack == null)
            return false;
        ToolStack helmet = ToolStack.from(headStack);
        return this.mob.getTarget() != null
                && helmet.getModifierLevel(TinkerModifiers.firebreath.get()) > 0
                && !this.mob.hasEffect(TinkerModifiers.fireballCooldownEffect.get())
                && !this.mob.isInWaterRainOrBubble();
    }

    @Nullable
    protected ItemStack getHelmet() {
        if (this.mob.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SlimeskullItem)
            return this.mob.getItemBySlot(EquipmentSlot.HEAD);
        return null;
    }

    public void start() {
        super.start();
        if (this.mob.getTarget() == null)
            return;

        this.useFireBreath();
        this.stop();
    }

    //Copy of FirebreathModifier.startInteract but adapted for the mob
    public void useFireBreath() {
        boolean hasFireball = false;
        ItemStack stack = this.mob.getOffhandItem();
        if (!stack.isEmpty() && stack.is(TinkerTags.Items.FIREBALLS)) {
            hasFireball = true;
            stack.shrink(1);
            if (stack.isEmpty())
                this.mob.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        // if we found a fireball, fire it
        if (hasFireball) {
            this.mob.playSound(SoundEvents.BLAZE_SHOOT, 2.0F, (this.mob.getRandom().nextFloat() - this.mob.getRandom().nextFloat()) * 0.2F + 1.0F);
            Vec3 lookVec = this.mob.getLookAngle().multiply(2.0f, 2.0f, 2.0f);
            SmallFireball fireball = new SmallFireball(this.mob.level, this.mob, lookVec.x + this.mob.getRandom().nextGaussian() / 16, lookVec.y, lookVec.z + this.mob.getRandom().nextGaussian() / 16);
            fireball.setPos(fireball.getX(), this.mob.getY(0.5D) + 0.5D, fireball.getZ());
            this.mob.level.addFreshEntity(fireball);
            TinkerModifiers.fireballCooldownEffect.get().apply(this.mob, 100, 0, true);
        }
    }
}
