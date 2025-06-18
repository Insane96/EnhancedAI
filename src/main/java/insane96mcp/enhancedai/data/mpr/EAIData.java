package insane96mcp.enhancedai.data.mpr;

import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class EAIData<T> {
    public static final List<EAIData<?>> DATA = new ArrayList<>();

    private final ResourceLocation id;
    private final BiConsumer<Mob, T> consumer;
    private final Class<T> type;

    private EAIData(ResourceLocation id, BiConsumer<Mob, T> consumer, Class<T> type) {
        this.id = id;
        this.consumer = consumer;
        this.type = type;
    }

    public void consume(Mob mob, T value) {
        ModNBTData.put(mob, this.id, value);
        consumer.accept(mob, value);
    }

    public ResourceLocation id() {
        return id;
    }

    public BiConsumer<Mob, T> consumer() {
        return consumer;
    }

    public Class<T> type() {
        return type;
    }

    public static EAIData<Boolean> ofBool(ResourceLocation id, BiConsumer<Mob, Boolean> consumer) {
        var data = new EAIData<>(id, consumer, Boolean.class);
        DATA.add(data);
        return data;
    }

    public static EAIData<Integer> ofInt(ResourceLocation id, BiConsumer<Mob, Integer> consumer) {
        var data = new EAIData<>(id, consumer, Integer.class);
        DATA.add(data);
        return data;
    }

    public static EAIData<Double> ofDouble(ResourceLocation id, BiConsumer<Mob, Double> consumer) {
        var data = new EAIData<>(id, consumer, Double.class);
        DATA.add(data);
        return data;
    }

    public T get(LivingEntity entity) {
        return ModNBTData.get(entity, this.id, this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EAIData) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, consumer);
    }

    @Override
    public String toString() {
        return "EAIData[" +
                "id=" + id + ", " +
                "consumer=" + consumer + ']';
    }

    public T parse(String input) {
        if (type == Boolean.class)
            return type.cast(Boolean.parseBoolean(input));
        else if (type == Integer.class)
            return type.cast(Integer.parseInt(input));
        else if (type == Double.class)
            return type.cast(Double.parseDouble(input));
        else
            throw new IllegalStateException("Unsupported type: " + type);
    }
}
