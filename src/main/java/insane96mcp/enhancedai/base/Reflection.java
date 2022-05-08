package insane96mcp.enhancedai.base;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class Reflection {

	public static final MethodHandle FIRE_ARROW = createMethodHandle(AbstractSkeleton.class, "getArrow", ItemStack.class, float.class);

	private static MethodHandle createMethodHandle(@Nonnull final Class<?> clazz, @Nonnull final String methodName, @Nonnull final Class<?>... parameterTypes) {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodHandle ret = null;
		try {
			ret = lookup.unreflect(ObfuscationReflectionHelper.findMethod(clazz, methodName, parameterTypes));
		}
		catch (IllegalAccessException exception) {

		}
		return ret;
	}

	public static void invoke(MethodHandle methodHandle, Object... args) {
		try {
			methodHandle.invoke(args);
		}
		catch (Throwable throwable) {
			EnhancedAI.LOGGER.error(throwable.toString());
		}
	}

	@Nullable
	public static Object invokeWithReturn(MethodHandle methodHandle, Object... args) {
		Object ret = null;
		try {
			ret = methodHandle.invoke(args);
		}
		catch (Throwable throwable) {
			EnhancedAI.LOGGER.error(throwable.toString());
		}
		return ret;
	}

}
