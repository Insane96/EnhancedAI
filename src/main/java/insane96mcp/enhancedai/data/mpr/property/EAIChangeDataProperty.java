package insane96mcp.enhancedai.data.mpr.property;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import insane96mcp.mobspropertiesrandomness.data.json.property.MPRProperty;
import insane96mcp.mobspropertiesrandomness.data.json.util.modifiable.MPRRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@JsonAdapter(EAIChangeDataProperty.Serializer.class)
public class EAIChangeDataProperty extends MPRProperty {
    public ResourceLocation data;
    @Nullable
    public String stringValue;
    @Nullable
    public MPRRange value;

    public EAIChangeDataProperty(ResourceLocation data, @Nullable String stringValue, @Nullable MPRRange value, List<MPRCondition> conditions) {
        super(conditions);
        this.data = data;
        this.stringValue = stringValue;
        this.value = value;
    }

    @Override
    public boolean apply(LivingEntity living) {
        if (!(living instanceof Mob mob))
            return false;
        Optional<EAIData<?>> optData = EAIData.DATA.stream().filter(d -> d.id().equals(this.data)).findFirst();
        if (optData.isEmpty())
            return false;

        EAIData<?> data = optData.get();
        if (data.type() == List.class)
            throw new UnsupportedOperationException("List Data are not supported by change_data property");

        if (this.value != null) {
            if (data.type() == Boolean.class)
                EAIData.apply(data, mob, this.value.getDoubleBetween(living) >= 1d);
            else
                EAIData.apply(data, mob, this.value.getDoubleBetween(living));
        }
        else if (this.stringValue != null)
            EAIData.apply(data, mob, this.stringValue);
        return true;
    }

    public static class Serializer implements JsonDeserializer<EAIChangeDataProperty>, JsonSerializer<EAIChangeDataProperty> {
        @Override
        public EAIChangeDataProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            ResourceLocation data = ResourceLocation.parse(GsonHelper.getAsString(jObject, "data"));
            Optional<EAIData<?>> optData = EAIData.DATA.stream().filter(d -> d.id().equals(data)).findFirst();
            if (optData.isEmpty())
                throw new JsonParseException("Defined data does not exist: " + data);
            return new EAIChangeDataProperty(data, GsonHelper.getAsString(jObject, "string_value", null), GsonHelper.getAsObject(jObject, "value", null, context, MPRRange.class), MPRCondition.deserializeConditions(jObject, context));
        }

        @Override
        public JsonElement serialize(EAIChangeDataProperty src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("data", src.data.toString());
            if (src.value != null)
                jObject.add("value", context.serialize(src.value));
            if (src.stringValue != null)
                jObject.addProperty("string_value", src.stringValue);
            return src.endSerialization(jObject, context);
        }
    }
}
