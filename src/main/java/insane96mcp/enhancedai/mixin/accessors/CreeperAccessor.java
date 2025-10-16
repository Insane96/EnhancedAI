package insane96mcp.enhancedai.mixin.accessors;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperAccessor {
	@Accessor
	int getMaxSwell();

	@Accessor
	int getExplosionRadius();
}
