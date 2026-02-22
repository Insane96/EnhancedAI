package insane96mcp.enhancedai.data;

import insane96mcp.insanelib.core.ModNBTData;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EAIDataList<T> extends EAIData<List<T>> {

	private final Class<T> elementType;

	private EAIDataList(ResourceLocation id, BiConsumer<Mob, List<T>> onChange, Class<T> elementType) {
		super(id, onChange, (Class<List<T>>) (Class<?>) List.class);
		this.elementType = elementType;
	}

	@Override
	public void apply(Mob mob, List<T> value) {
		ListTag listTag = toListTag(value);
		ModNBTData.put(mob, this.id(), listTag);
		onChange().accept(mob, value);
	}

	@Override
	public List<T> get(Entity entity) {
		int nbtType = nbtTypeFor(elementType);
		ListTag listTag = ModNBTData.getList(entity, this.id(), nbtType);
		return fromListTag(listTag);
	}

	private ListTag toListTag(List<T> list) {
		ListTag listTag = new ListTag();
		if (elementType == String.class)
			for (T e : list) listTag.add(StringTag.valueOf((String) e));
		else if (elementType == Integer.class)
			for (T e : list) listTag.add(IntTag.valueOf((Integer) e));
		else if (elementType == Double.class)
			for (T e : list) listTag.add(DoubleTag.valueOf((Double) e));
		else if (elementType == Boolean.class)
			for (T e : list) listTag.add(ByteTag.valueOf((Boolean) e ? (byte) 1 : (byte) 0));
		else
			throw new IllegalArgumentException("Unsupported element type: " + elementType);
		return listTag;
	}

	private List<T> fromListTag(ListTag listTag) {
		List<T> list = new ArrayList<>();
		if (elementType == String.class)
			for (Tag t : listTag) list.add(elementType.cast(t.getAsString()));
		else if (elementType == Integer.class)
			for (Tag t : listTag) list.add(elementType.cast(((IntTag) t).getAsInt()));
		else if (elementType == Double.class)
			for (Tag t : listTag) list.add(elementType.cast(((DoubleTag) t).getAsDouble()));
		else if (elementType == Boolean.class)
			for (Tag t : listTag) list.add(elementType.cast(((ByteTag) t).getAsByte() != 0));
		else
			throw new IllegalArgumentException("Unsupported element type: " + elementType);
		return list;
	}

	private int nbtTypeFor(Class<T> clazz) {
		if (clazz == String.class) return Tag.TAG_STRING;
		if (clazz == Integer.class) return Tag.TAG_INT;
		if (clazz == Double.class) return Tag.TAG_DOUBLE;
		if (clazz == Boolean.class) return Tag.TAG_BYTE;
		throw new IllegalArgumentException("Unsupported element type: " + clazz);
	}

	public static <E> EAIDataList<E> of(ResourceLocation id, Class<E> elementType) {
		return of(id, elementType, (mob, list) -> {});
	}

	public static <E> EAIDataList<E> of(ResourceLocation id, Class<E> elementType, BiConsumer<Mob, List<E>> onChange) {
		EAIDataList<E> data = new EAIDataList<>(id, onChange, elementType);
		DATA.add(data);
		return data;
	}

	public Class<T> elementType() {
		return elementType;
	}

	@Override
	public List<T> parse(String input) {
		String[] parts = input.split(",");
		List<T> result = new ArrayList<>();
		for (String part : parts) {
			if (elementType == String.class)
				result.add(elementType.cast(part.trim()));
			else if (elementType == Integer.class)
				result.add(elementType.cast(Integer.parseInt(part.trim())));
			else if (elementType == Double.class)
				result.add(elementType.cast(Double.parseDouble(part.trim())));
			else if (elementType == Boolean.class)
				result.add(elementType.cast(Boolean.parseBoolean(part.trim())));
			else
				throw new IllegalStateException("Unsupported element type: " + elementType);
		}
		return result;
	}
}