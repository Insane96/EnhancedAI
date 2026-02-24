package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.enhancedai.modules.mobs.FireImmuneTicks;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyReturnValue(method = "getFireImmuneTicks", at = @At(value = "RETURN"))
    public int enhancedai$getFireImmuneTicks(int original) {
        if (!Feature.isEnabled(FireImmuneTicks.class)
                || !FireImmuneTicks.FIRE_IMMUNE_TICKS.has(self()))
            return original;
        return FireImmuneTicks.FIRE_IMMUNE_TICKS.get(self());
    }

    @Unique
    public Entity self() {
        return (Entity) (Object) this;
    }
}
