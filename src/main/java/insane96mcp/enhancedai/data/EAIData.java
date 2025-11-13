package insane96mcp.enhancedai.data;

import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class EAIData<T> {
    public static final List<EAIData<?>> DATA = new ArrayList<>();

    protected final ResourceLocation id;
    protected final BiConsumer<Mob, T> onChange;
    protected final Class<T> type;

    protected EAIData(ResourceLocation id, BiConsumer<Mob, T> onChange, Class<T> type) {
        this.id = id;
        this.onChange = onChange;
        this.type = type;
    }

    public void apply(Mob mob, T value) {
        ModNBTData.put(mob, this.id, value);
        onChange.accept(mob, value);
    }

    /**
     * If this data is present, onChange is run
     */
    public void changed(Mob mob) {
        if (!ModNBTData.contains(mob, this.id))
            return;
        this.onChange.accept(mob, this.get(mob));
    }

	/// Adds NBT if not present and runs onChange
    public void applyIfAbsent(Mob mob, T value) {
        if (!ModNBTData.contains(mob, this.id))
            apply(mob, value);
        else
            onChange.accept(mob, get(mob));
    }

    public ResourceLocation id() {
        return id;
    }

    public BiConsumer<Mob, T> onChange() {
        return onChange;
    }

    public Class<T> type() {
        return type;
    }

    public static EAIData<Boolean> ofBool(ResourceLocation id) {
        return ofBool(id, (mob, value) -> {});
    }

    public static EAIData<Integer> ofInt(ResourceLocation id) {
        return ofInt(id, (mob, value) -> {});
    }

    public static EAIData<Double> ofDouble(ResourceLocation id) {
        return ofDouble(id, (mob, value) -> {});
    }

    public static EAIData<String> ofString(ResourceLocation id) {
        return ofString(id, (mob, value) -> {});
    }

    public static EAIData<Boolean> ofBool(ResourceLocation id, BiConsumer<Mob, Boolean> onChange) {
        var data = new EAIData<>(id, onChange, Boolean.class);
        DATA.add(data);
        return data;
    }

    public static EAIData<Integer> ofInt(ResourceLocation id, BiConsumer<Mob, Integer> onChange) {
        var data = new EAIData<>(id, onChange, Integer.class);
        DATA.add(data);
        return data;
    }

    public static EAIData<Double> ofDouble(ResourceLocation id, BiConsumer<Mob, Double> onChange) {
        var data = new EAIData<>(id, onChange, Double.class);
        DATA.add(data);
        return data;
    }

    public static EAIData<String> ofString(ResourceLocation id, BiConsumer<Mob, String> onChange) {
        var data = new EAIData<>(id, onChange, String.class);
        DATA.add(data);
        return data;
    }

	public static EAIData<List<String>> ofStringList(ResourceLocation id) {
		return ofStringList(id, (mob, value) -> {});
	}

	public static EAIData<List<String>> ofStringList(ResourceLocation id, BiConsumer<Mob, List<String>> onChange) {
		var data = new EAIData<>(id, onChange, (Class<List<String>>) (Class<?>) List.class);
		DATA.add(data);
		return data;
	}

    public T get(Entity entity) {
        return ModNBTData.get(entity, this.id, this.type);
    }

    public boolean has(Entity entity) {
        return ModNBTData.contains(entity, this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EAIData) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.onChange, that.onChange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, onChange);
    }

    @Override
    public String toString() {
        return "EAIData[" +
                "id=" + id + ", " +
                "consumer=" + onChange + ']';
    }

    public T parse(String input) {
        if (type == String.class)
            return type.cast(input);
        if (type == Boolean.class)
            return type.cast(parseBoolean(input));
        if (type == Integer.class)
            return type.cast(Integer.parseInt(input));
        if (type == Double.class)
            return type.cast(Double.parseDouble(input));
		if (type == List.class) {
			List<String> list = new ArrayList<>();
			for (String s : input.split(","))
				list.add(s.trim());
			return type.cast(list);
		}
		else
            throw new IllegalStateException("Unsupported type: " + type);
    }

    private boolean parseBoolean(String input) {
        if (!input.equalsIgnoreCase("true") && !input.equalsIgnoreCase("false"))
            throw new IllegalArgumentException("Invalid boolean value: " + input);
        return Boolean.parseBoolean(input);
    }

    @SuppressWarnings("unchecked")
    public static <T> void apply(EAIData<T> data, Mob mob, Object value) {
        data.apply(mob, (T) value);
    }
}
