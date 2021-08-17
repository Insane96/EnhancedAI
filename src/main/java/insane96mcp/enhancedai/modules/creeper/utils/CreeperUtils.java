package insane96mcp.enhancedai.modules.creeper.utils;

import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;

public class CreeperUtils {
	public static float getExplosionSize(CreeperEntity creeper) {
		CompoundNBT compoundNBT = new CompoundNBT();
		creeper.writeAdditional(compoundNBT);
		float explosionSize = compoundNBT.getByte("ExplosionRadius");
		explosionSize *= compoundNBT.getBoolean("powered") ? 2 : 1;
		return explosionSize;
	}

	public static float getExplosionSizeSq(CreeperEntity creeper) {
		float explosionSize = getExplosionSize(creeper);
		return explosionSize * explosionSize;
	}
}
