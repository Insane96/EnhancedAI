package insane96mcp.enhancedai.data;

import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.function.BiConsumer;

public class EAIDataEnum<T extends Enum<T>> extends EAIData<T> {

	private final Class<T> enumType;

	private EAIDataEnum(ResourceLocation id, BiConsumer<Mob, T> onChange, Class<T> enumType) {
		super(id, onChange, enumType);
		this.enumType = enumType;
	}

	public static <E extends Enum<E>> EAIDataEnum<E> of(ResourceLocation id, Class<E> enumType) {
		return of(id, (mob, enumValue) -> {}, enumType);
	}

	public static <E extends Enum<E>> EAIDataEnum<E> of(ResourceLocation id, BiConsumer<Mob, E> onChange, Class<E> enumType) {
		EAIDataEnum<E> data = new EAIDataEnum<>(id, onChange, enumType);
		DATA.add(data);
		return data;
	}

	@Override
	public void apply(Mob mob, T value) {
		ModNBTData.put(mob, this.id, value.name());
		onChange.accept(mob, value);
	}

	@Override
	public T get(Entity entity) {
		if (!ModNBTData.contains(entity, this.id))
			return this.getDefault();
		String stored = ModNBTData.get(entity, this.id, String.class);
		try {
			return Enum.valueOf(enumType, stored);
		} catch (IllegalArgumentException e) {
			return this.getDefault();
		}
	}

	@Override
	public T parse(String input) {
		if (input == null || input.isBlank())
			return super.parse(input);
		try {
			return Enum.valueOf(enumType, input);
		} catch (IllegalArgumentException e) {
			return super.parse(input);
		}
	}

	private T getDefault() {
		return enumType.getEnumConstants()[0];
	}
}