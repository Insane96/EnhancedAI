package insane96mcp.enhancedai.setup;

import insane96mcp.insanelib.core.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NBTUtils {
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
