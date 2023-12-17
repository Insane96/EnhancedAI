package insane96mcp.enhancedai.modules.mobs.targeting;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(CustomHostileConfig.Serializer.class)
public class CustomHostileConfig {
    public IdTagMatcher attacker;
    public IdTagMatcher victim;
    public double chance;

    public CustomHostileConfig(IdTagMatcher attacker, IdTagMatcher victim, double chance) {
        this.attacker = attacker;
        this.victim = victim;
        this.chance = chance;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<CustomHostileConfig>>(){}.getType();

    public static class Serializer implements JsonDeserializer<CustomHostileConfig>, JsonSerializer<CustomHostileConfig> {
        @Override
        public CustomHostileConfig deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            IdTagMatcher attacker = context.deserialize(jObject.get("attacker"), IdTagMatcher.class);
            IdTagMatcher victim = context.deserialize(jObject.get("victim"), IdTagMatcher.class);
            double chance = GsonHelper.getAsDouble(jObject, "chance", 1f);

            return new CustomHostileConfig(attacker, victim, chance);
        }

        @Override
        public JsonElement serialize(CustomHostileConfig src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("attacker", context.serialize(src.attacker));
            jObject.add("victim", context.serialize(src.victim));
            jObject.addProperty("chance", src.chance);
            return jObject;
        }
    }
}
