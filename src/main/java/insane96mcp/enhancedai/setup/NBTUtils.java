package insane96mcp.enhancedai.setup;

import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NBTUtils {
    /**
     * Returns the int read from the compoundTag or if absent puts the defaultValue in the compoundTag and returns it
     */
    @Deprecated
    public static int getIntOrPutDefaultLegacy(CompoundTag compoundTag, String tagName, int defaultValue) {
        int result = defaultValue;
        if (compoundTag.contains(tagName)) {
            result = compoundTag.getInt(tagName);
        }
        else {
            compoundTag.putInt(tagName, defaultValue);
        }
        return result;
    }

    /**
     * Returns the double read from the compoundTag or if absent puts the defaultValue in the compoundTag and returns it
     */
    @Deprecated
    public static double getDoubleOrPutDefaultLegacy(CompoundTag compoundTag, String tagName, double defaultValue) {
        double result = defaultValue;
        if (compoundTag.contains(tagName)) {
            result = compoundTag.getDouble(tagName);
        }
        else {
            compoundTag.putDouble(tagName, defaultValue);
        }
        return result;
    }

    /**
     * Returns the boolean read from the compoundTag or if absent puts the defaultValue in the compoundTag and returns it
     */
    @Deprecated
    public static boolean getBooleanOrPutDefaultLegacy(CompoundTag compoundTag, String tagName, boolean defaultValue) {
        boolean result = defaultValue;
        if (compoundTag.contains(tagName)) {
            result = compoundTag.getBoolean(tagName);
        }
        else {
            compoundTag.putBoolean(tagName, defaultValue);
        }
        return result;
    }

    /**
     * Returns the boolean read from the compoundTag or if absent, puts the defaultValue in the compoundTag and returns it
     */
    public static boolean getBooleanOrPutDefault(Entity entity, ResourceLocation key, boolean defaultValue) {
        if (!ModNBTData.contains(entity, key)) {
            ModNBTData.put(entity, key, defaultValue);
            return defaultValue;
        }
        return ModNBTData.get(entity, key, Boolean.class);
    }

    /**
     * Returns the boolean read from the compoundTag or if absent, puts the defaultValue in the compoundTag and returns it
     */
    public static int getIntOrPutDefault(Entity entity, ResourceLocation key, int defaultValue) {
        if (!ModNBTData.contains(entity, key)) {
            ModNBTData.put(entity, key, defaultValue);
            return defaultValue;
        }
        return ModNBTData.get(entity, key, Integer.class);
    }

    /**
     * Returns the boolean read from the compoundTag or if absent, puts the defaultValue in the compoundTag and returns it
     */
    public static double getDoubleOrPutDefault(Entity entity, ResourceLocation key, double defaultValue) {
        if (!ModNBTData.contains(entity, key)) {
            ModNBTData.put(entity, key, defaultValue);
            return defaultValue;
        }
        return ModNBTData.get(entity, key, Double.class);
    }
}
