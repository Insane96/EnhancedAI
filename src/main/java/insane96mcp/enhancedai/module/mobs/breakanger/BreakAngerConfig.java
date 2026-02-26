package insane96mcp.enhancedai.module.mobs.breakanger;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;

@JsonAdapter(BreakAngerConfig.Serializer.class)
public class BreakAngerConfig {
    public ObjTag<Block> block;
    public ObjTag<EntityType<?>> entity;
    public double range;
    public boolean requiresLineOfSight;

    public BreakAngerConfig(ObjTag<Block> block, ObjTag<EntityType<?>> entity, double range, boolean requiresLineOfSight) {
        this.block = block;
        this.entity = entity;
        this.range = range;
        this.requiresLineOfSight = requiresLineOfSight;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<BreakAngerConfig>>(){}.getType();

    public static class Serializer implements JsonDeserializer<BreakAngerConfig>, JsonSerializer<BreakAngerConfig> {
        @Override
        public BreakAngerConfig deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            ObjTag<Block> block = ObjTag.deserialize(jObject.get("block"), Registries.BLOCK);
            ObjTag<EntityType<?>> entity = ObjTag.deserialize(jObject.get("entity"), Registries.ENTITY_TYPE);
            double range = GsonHelper.getAsDouble(jObject, "range");
            boolean requiresLineOfSight = GsonHelper.getAsBoolean(jObject, "requires_line_of_sight", false);

            return new BreakAngerConfig(block, entity, range, requiresLineOfSight);
        }

        @Override
        public JsonElement serialize(BreakAngerConfig src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.add("block", context.serialize(src.block));
            jObject.add("entity", context.serialize(src.entity));
            jObject.addProperty("range", src.range);
            jObject.addProperty("requires_line_of_sight", src.requiresLineOfSight);
            return jObject;
        }
    }
}
