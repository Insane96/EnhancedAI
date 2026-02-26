package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.enhancedai.module.mobs.Swimmers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFish.class)
public abstract class AbstractFishMixin extends WaterAnimal {
    protected AbstractFishMixin(EntityType<? extends WaterAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyExpressionValue(method = "travel", at = @At(value = "CONSTANT", args = "floatValue=0.01"))
    public float enhancedai$increaseSwimSpeed(float original) {
        return Swimmers.fishSwimSpeed(original);
    }
}
