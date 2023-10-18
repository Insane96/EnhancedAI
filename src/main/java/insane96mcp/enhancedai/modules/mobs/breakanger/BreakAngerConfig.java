package insane96mcp.enhancedai.modules.mobs.breakanger;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(BreakAngerConfig.Serializer.class)
public class BreakAngerConfig {
    public IdTagMatcher block;
    public IdTagMatcher entity;
    public double range;

    public BreakAngerConfig(IdTagMatcher block, IdTagMatcher entity, double range) {
        this.block = block;
        this.entity = entity;
        this.range = range;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<BreakAngerConfig>>(){}.getType();

    public static class Serializer implements JsonDeserializer<BreakAngerConfig>, JsonSerializer<BreakAngerConfig> {
        @Override
        public BreakAngerConfig deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            IdTagMatcher block = context.deserialize(jObject.get("block"), IdTagMatcher.class);
            IdTagMatcher entity = context.deserialize(jObject.get("entity"), IdTagMatcher.class);
            double range = GsonHelper.getAsDouble(jObject, "range");

            return new BreakAngerConfig(block, entity, range);
        }

        @Override
        public JsonElement serialize(BreakAngerConfig src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("block", context.serialize(src.block));
            jObject.add("entity", context.serialize(src.entity));
            jObject.add("block", context.serialize(src.block));
            jObject.addProperty("range", src.range);
            return jObject;
        }
    }
}
