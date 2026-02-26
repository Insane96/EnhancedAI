package insane96mcp.enhancedai.data.mpr.condition;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.enhancedai.module.mobs.Leaders;
import insane96mcp.mobspropertiesrandomness.data.json.condition.MPRCondition;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Type;

@JsonAdapter(EAIIsLeaderCondition.Serializer.class)
public class EAIIsLeaderCondition extends MPRCondition {
    public EAIIsLeaderCondition(boolean inverted) {
        super(inverted);
    }

    protected boolean conditionCheck(LivingEntity living) {
        return Leaders.isLeader(living);
    }

    public static class Serializer implements JsonDeserializer<EAIIsLeaderCondition>, JsonSerializer<EAIIsLeaderCondition> {
        public EAIIsLeaderCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new EAIIsLeaderCondition(MPRCondition.deserializeInverted(jObject));
        }

        public JsonElement serialize(EAIIsLeaderCondition src, Type typeOfSrc, JsonSerializationContext context) {
            return src.endSerialization(new JsonObject());
        }
    }
}
