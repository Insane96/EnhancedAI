package insane96mcp.enhancedai.utils;

import insane96mcp.enhancedai.EnhancedAI;

public class LogHelper {
	public static void error(String format, Object... args) {
		EnhancedAI.LOGGER.error(String.format(format, args));
	}

	public static void warn(String format, Object... args) {
		EnhancedAI.LOGGER.warn(String.format(format, args));
	}

	public static void info(String format, Object... args) {
		EnhancedAI.LOGGER.info(String.format(format, args));
	}
}
