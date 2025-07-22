package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.enhancedai.modules.drowned.Drowned;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(net.minecraft.world.entity.monster.Drowned.class)
public abstract class DrownedMixin extends Zombie {
    public DrownedMixin(EntityType<? extends Zombie> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyReturnValue(method = "okTarget", at = @At(value = "RETURN", ordinal = 0))
    public boolean enhancedai$okTarget(boolean original) {
        if (!Drowned.allowAttackDuringDay())
            return original;
        return original || this.level().isDay();
    }
}
