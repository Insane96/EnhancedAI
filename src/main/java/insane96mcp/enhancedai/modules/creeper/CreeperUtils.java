package insane96mcp.enhancedai.modules.creeper;

import insane96mcp.enhancedai.mixin.accessors.CreeperAccessor;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperUtils {
	public static float getExplosionSize(Creeper creeper) {
		float explosionSize = ((CreeperAccessor)creeper).getExplosionRadius();
		explosionSize *= creeper.isPowered() ? 2 : 1;
		return explosionSize;
	}

	public static int getFuse(Creeper creeper) {
		return ((CreeperAccessor)creeper).getMaxSwell();
	}
}
