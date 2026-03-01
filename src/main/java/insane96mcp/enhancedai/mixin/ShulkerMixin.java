package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.module.shulker.ShulkerArmor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Shulker.class)
public class ShulkerMixin extends AbstractGolem {

    @Shadow @Final private static AttributeModifier COVERED_ARMOR_MODIFIER;

    protected ShulkerMixin(EntityType<? extends AbstractGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "setRawPeekAmount", at = @At("RETURN"))
    public void onSetRawPeekAmount(int peekAmount, CallbackInfo ci) {
        if (this.level().isClientSide
                || !ShulkerArmor.isAffectedByArmorModifiers(self()))
            return;

        AttributeInstance armorAttribute = this.getAttribute(Attributes.ARMOR);
        armorAttribute.removeModifier(COVERED_ARMOR_MODIFIER);
        armorAttribute.removeModifier(ShulkerArmor.CLOSED_MODIFIER);
        armorAttribute.removeModifier(ShulkerArmor.OPEN_MODIFIER);
        armorAttribute.removeModifier(ShulkerArmor.PEEK_MODIFIER);
        if (peekAmount == 0)
            armorAttribute.addPermanentModifier(ShulkerArmor.CLOSED_MODIFIER);
        else if (peekAmount == 100)
            armorAttribute.addPermanentModifier(ShulkerArmor.OPEN_MODIFIER);
        else
            armorAttribute.addPermanentModifier(ShulkerArmor.PEEK_MODIFIER);
    }

    public Shulker self() {
        return (Shulker) (Object) this;
    }
}
