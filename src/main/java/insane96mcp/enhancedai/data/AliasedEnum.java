package insane96mcp.enhancedai.data;

import java.util.Map;

/**
 * Implemented by enums that have been renamed, so that old names (stored in NBT, data keys, etc.) still resolve to the new values
 * @deprecated Only for compatibility with old names, to be removed in 26.1
 */
@Deprecated(since = "4.2.6.0", forRemoval = true)
public interface AliasedEnum<E extends Enum<E>> {
	/**
	 * Legacy names mapped to the current values
	 */
	Map<String, E> aliases();

	/**
	 * Like {@link Enum#valueOf(Class, String)} but also resolves the aliases if the enum implements {@link AliasedEnum}
	 */
	@SuppressWarnings("unchecked")
	static <E extends Enum<E>> E valueOf(Class<E> enumType, String name) {
		try {
			return Enum.valueOf(enumType, name);
		}
		catch (IllegalArgumentException e) {
			if (enumType.getEnumConstants()[0] instanceof AliasedEnum<?> aliasedEnum) {
				Enum<?> alias = aliasedEnum.aliases().get(name);
				if (alias != null)
					return (E) alias;
			}
			throw e;
		}
	}
}
