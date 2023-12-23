package insane96mcp.enhancedai.modules.mobs.targeting;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(CustomHostileConfig.Serializer.class)
public class CustomHostileConfig {
    public int priority;
    public IdTagMatcher attacker;
    public IdTagMatcher victim;
    public double chance;
    public boolean mustSee;

    public CustomHostileConfig(int priority, IdTagMatcher attacker, IdTagMatcher victim, double chance) {
        this(priority, attacker, victim, chance, true);
    }

    public CustomHostileConfig(int priority, IdTagMatcher attacker, IdTagMatcher victim, double chance, boolean mustSee) {
        this.priority = priority;
        this.attacker = attacker;
        this.victim = victim;
        this.chance = chance;
        this.mustSee = mustSee;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<CustomHostileConfig>>(){}.getType();

    public static class Serializer implements JsonDeserializer<CustomHostileConfig>, JsonSerializer<CustomHostileConfig> {
        @Override
        public CustomHostileConfig deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            int priority = GsonHelper.getAsInt(jObject, "priority");
            IdTagMatcher attacker = context.deserialize(jObject.get("attacker"), IdTagMatcher.class);
            IdTagMatcher victim = context.deserialize(jObject.get("victim"), IdTagMatcher.class);
            double chance = GsonHelper.getAsDouble(jObject, "chance", 1f);
            boolean mustSee = GsonHelper.getAsBoolean(jObject, "requires_line_of_sight", true);

            return new CustomHostileConfig(priority, attacker, victim, chance, mustSee);
        }

        @Override
        public JsonElement serialize(CustomHostileConfig src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("priority", src.priority);
            jObject.add("attacker", context.serialize(src.attacker));
            jObject.add("victim", context.serialize(src.victim));
            if (src.chance < 1f)
                jObject.addProperty("chance", src.chance);
            if (!src.mustSee)
                jObject.addProperty("requires_line_of_sight", src.mustSee);
            return jObject;
        }
    }
}
