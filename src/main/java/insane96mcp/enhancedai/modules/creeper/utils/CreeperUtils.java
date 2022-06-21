package insane96mcp.enhancedai.modules.creeper.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperUtils {
	public static float getExplosionSize(Creeper creeper) {
		CompoundTag compoundNBT = new CompoundTag();
		creeper.addAdditionalSaveData(compoundNBT);
		float explosionSize = compoundNBT.getByte("ExplosionRadius");
		explosionSize *= compoundNBT.getBoolean("powered") ? 2 : 1;
		return explosionSize;
	}

	public static float getExplosionSizeSqr(Creeper creeper) {
		return (float) Math.pow(getExplosionSize(creeper), 2);
	}

	public static short getFuse(Creeper creeper) {
		CompoundTag compoundNBT = new CompoundTag();
		creeper.addAdditionalSaveData(compoundNBT);
		return compoundNBT.getShort("Fuse");
	}

	public static void setFuseTime(Creeper creeper, short fuse) {
		CompoundTag compoundNBT = new CompoundTag();
		compoundNBT.putShort("Fuse", fuse);
		creeper.readAdditionalSaveData(compoundNBT);
	}
}
