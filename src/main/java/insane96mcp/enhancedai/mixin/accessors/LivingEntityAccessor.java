package insane96mcp.enhancedai.mixin.accessors;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor("lastDamageSource")
	void setLastDamageSource(DamageSource source);
	@Accessor("lastDamageStamp")
	void setLastDamageStamp(long stamp);
}
