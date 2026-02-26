package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.enhancedai.module.mobs.climbing.Climbing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyReturnValue(method = "onClimbable", at = @At(value = "RETURN", ordinal = 1))
    public boolean enhancedai$isOnClimbable(boolean original) {
        if (!Climbing.CAN_CLIMB_WALLS_DATA.get(this))
            return original;
        return this.horizontalCollision;
    }
}
