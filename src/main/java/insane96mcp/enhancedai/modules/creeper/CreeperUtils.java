package insane96mcp.enhancedai.modules.creeper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;

public class CreeperUtils {
	public static float getExplosionSize(Mob creeper) {
		CompoundTag compoundNBT = new CompoundTag();
		creeper.addAdditionalSaveData(compoundNBT);
		float explosionSize = compoundNBT.getByte("ExplosionRadius");
		explosionSize *= compoundNBT.getBoolean("powered") ? 2 : 1;
		return explosionSize;
	}

	public static float getExplosionSizeSqr(Mob creeper) {
		return (float) Math.pow(getExplosionSize(creeper), 2);
	}

	public static short getFuse(Mob creeper) {
		CompoundTag compoundNBT = new CompoundTag();
		creeper.addAdditionalSaveData(compoundNBT);
		return compoundNBT.getShort("Fuse");
	}

	public static void setFuseTime(Mob creeper, short fuse) {
		CompoundTag compoundNBT = new CompoundTag();
		compoundNBT.putShort("Fuse", fuse);
		creeper.readAdditionalSaveData(compoundNBT);
	}
}
