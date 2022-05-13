package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.utils.LogHelper;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;

public class Reflection {
    static Method AbstractSkeleton_getArrow;

    public static AbstractArrow AbstractSkeleton_getArrow(AbstractSkeleton skeleton, ItemStack stack, float damage) {
        try {
            return (AbstractArrow) AbstractSkeleton_getArrow.invoke(skeleton, stack, damage);
        }
        catch (Exception e) {
            LogHelper.error(e.toString());
        }
        return null;
    }

    public static void init() {
        AbstractSkeleton_getArrow = ObfuscationReflectionHelper.findMethod(AbstractSkeleton.class, "m_7932_", ItemStack.class, float.class);
        AbstractSkeleton_getArrow.setAccessible(true);
    }
}
