package insane96mcp.enhancedai.data.mpr.condition;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.mobspropertiesrandomness.data.json.util.modifiable.MPRRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;

@JsonAdapter(EAIDataCondition.Serializer.class)
public class EAIDataCondition extends MPRCondition {
    public ResourceLocation data;
    @Nullable
    public String stringValue;
    @Nullable
    public MPRRange value;

    public EAIDataCondition(ResourceLocation data, @Nullable String stringValue, @Nullable MPRRange value, boolean inverted) {
        super(inverted);
        this.data = data;
        this.stringValue = stringValue;
        this.value = value;
    }

    protected boolean conditionCheck(LivingEntity living) {
        Optional<EAIData<?>> optData = EAIData.DATA.stream().filter(d -> d.id().equals(this.data)).findFirst();
        if (optData.isEmpty()) return false;

        EAIData<?> data = optData.get();

        if (!data.has(living))
            return false;
        Object value = data.get(living);
        if (this.value != null) {
            if (value instanceof Double d)
                return this.value.isBetween(living, d);
            else if (value instanceof Integer i)
                return this.value.isBetween(living, i);
            else if (value instanceof Boolean b)
                return this.value.isBetween(living, b ? 1 : 0);
        }
        else if (value instanceof String && this.stringValue != null)
            return value.equals(this.stringValue);
        return false;
    }

    public static class Serializer implements JsonDeserializer<EAIDataCondition>, JsonSerializer<EAIDataCondition> {
        public EAIDataCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            ResourceLocation data = ResourceLocation.parse(GsonHelper.getAsString(jObject, "data"));
            Optional<EAIData<?>> optData = EAIData.DATA.stream().filter(d -> d.id().equals(data)).findFirst();
            if (optData.isEmpty())
                throw new JsonParseException("Defined data does not exist: " + data);
            return new EAIDataCondition(data, GsonHelper.getAsString(jObject, "string_value", null), GsonHelper.getAsObject(jObject, "value", null, context, MPRRange.class), MPRCondition.deserializeInverted(jObject));
        }

        public JsonElement serialize(EAIDataCondition src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("data", src.data.toString());
            if (src.value != null)
                jObject.add("value", context.serialize(src.value));
            if (src.stringValue != null)
                jObject.addProperty("string_value", src.stringValue);
            return src.endSerialization(jObject);
        }
    }
}
