package insane96mcp.enhancedai.modules.mobs.flee;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.IdTagMatcher;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;

@JsonAdapter(CustomFleeConfig.Serializer.class)
public class CustomFleeConfig {
	public int priority;
	public IdTagMatcher entity;
	public IdTagMatcher fleeFrom;
	public double chance;
	public double avoidDistance;
	public double avoidDistanceNear;
	public double speedMultiplier;
	public double speedMultiplierNear;

	public CustomFleeConfig(int priority, IdTagMatcher entity, IdTagMatcher fleeFrom, double chance, double avoidDistance, double avoidDistanceNear, double speedMultiplier, double speedMultiplierNear) {
		this.priority = priority;
		this.entity = entity;
		this.fleeFrom = fleeFrom;
		this.chance = chance;
		this.avoidDistance = avoidDistance;
		this.avoidDistanceNear = avoidDistanceNear;
		this.speedMultiplier = speedMultiplier;
		this.speedMultiplierNear = speedMultiplierNear;
	}

	public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<CustomFleeConfig>>(){}.getType();

	public static class Serializer implements JsonDeserializer<CustomFleeConfig>, JsonSerializer<CustomFleeConfig> {
		@Override
		public CustomFleeConfig deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jObject = json.getAsJsonObject();
			int priority = GsonHelper.getAsInt(jObject, "priority");
			IdTagMatcher entity = context.deserialize(jObject.get("entity"), IdTagMatcher.class);
			IdTagMatcher fleeFrom = context.deserialize(jObject.get("flee_from"), IdTagMatcher.class);
			double chance = GsonHelper.getAsDouble(jObject, "chance", 1f);
			double avoidDistance = GsonHelper.getAsDouble(jObject, "avoid_distance");
			double avoidDistanceNear = GsonHelper.getAsDouble(jObject, "avoid_distance_near");
			double speedMultiplier = GsonHelper.getAsDouble(jObject, "speed_multiplier");
			double speedMultiplierNear = GsonHelper.getAsDouble(jObject, "speed_multiplier_near");

			return new CustomFleeConfig(priority, entity, fleeFrom, chance, avoidDistance, avoidDistanceNear, speedMultiplier, speedMultiplierNear);
		}

		@Override
		public JsonElement serialize(CustomFleeConfig src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jObject = new JsonObject();
			jObject.addProperty("priority", src.priority);
			jObject.add("entity", context.serialize(src.entity));
			jObject.add("flee_from", context.serialize(src.fleeFrom));
			if (src.chance < 1f)
				jObject.addProperty("chance", src.chance);
			jObject.addProperty("avoid_distance", src.avoidDistance);
			jObject.addProperty("avoid_distance_near", src.avoidDistanceNear);
			jObject.addProperty("speed_multiplier", src.speedMultiplier);
			jObject.addProperty("speed_multiplier_near", src.speedMultiplierNear);
			return jObject;
		}
	}
}
