package insane96mcp.enhancedai.modules.creeper.utils;

import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;

public class CreeperUtils {
	public static float getExplosionSize(CreeperEntity creeper) {
		CompoundNBT compoundNBT = new CompoundNBT();
		creeper.addAdditionalSaveData(compoundNBT);
		float explosionSize = compoundNBT.getByte("ExplosionRadius");
		explosionSize *= compoundNBT.getBoolean("powered") ? 2 : 1;
		return explosionSize;
	}

	public static float getExplosionSizeSq(CreeperEntity creeper) {
		float explosionSize = getExplosionSize(creeper);
		return explosionSize * explosionSize;
	}

	public static short getFuse(CreeperEntity creeper) {
		CompoundNBT compoundNBT = new CompoundNBT();
		creeper.addAdditionalSaveData(compoundNBT);
		return compoundNBT.getShort("Fuse");
	}

	public static void setFuseTime(CreeperEntity creeper, short fuse) {
		CompoundNBT compoundNBT = new CompoundNBT();
		compoundNBT.putShort("Fuse", fuse);
		creeper.readAdditionalSaveData(compoundNBT);
	}
}
