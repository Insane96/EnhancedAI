package insane96mcp.enhancedai.module.mobs.customtargeting;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.enhancedai.module.mobs.targeting.EAINearestAttackableTarget;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.ArrayList;

@JsonAdapter(CustomHostileData.Serializer.class)
public class CustomHostileData {
    public int priority;
    public ObjTag<EntityType<?>> attacker;
    public ObjTag<EntityType<?>> target;
    public double chance;
    public boolean mustSee;
    public boolean addAttackGoal;

    public CustomHostileData(int priority, ObjTag<EntityType<?>> attacker, ObjTag<EntityType<?>> target) {
        this.priority = priority;
        this.attacker = attacker;
        this.target = target;
        this.chance = 1d;
        this.mustSee = true;
        this.addAttackGoal = false;
    }

    public CustomHostileData chance(double chance) {
        this.chance = chance;
        return this;
    }

    public CustomHostileData mustSee(boolean mustSee) {
        this.mustSee = mustSee;
        return this;
    }

    public CustomHostileData addAttackGoal() {
        this.addAttackGoal = true;
        return this;
    }

    public void tryApply(Mob mob) {
        if (!this.attacker.matches(mob.getType()) || !this.checkChance(mob.getRandom()))
            return;

        EAINearestAttackableTarget<LivingEntity> targetGoal = new EAINearestAttackableTarget<>(mob, LivingEntity.class, this.target, this.mustSee, false, TargetingConditions.forCombat());

        mob.targetSelector.addGoal(this.priority, targetGoal);

        if (this.addAttackGoal && mob instanceof PathfinderMob pathfinderMob) {
            mob.targetSelector.addGoal(1, (new HurtByTargetGoal(pathfinderMob)).setAlertOthers());
            //TODO Configurable speed modifier
            mob.goalSelector.addGoal(1, new MeleeAttackGoal(pathfinderMob, 1f, true));
        }
    }

    public boolean checkChance(RandomSource random) {
        return this.chance >= 1f || random.nextFloat() < this.chance;
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<CustomHostileData>>(){}.getType();

    public static class Serializer implements JsonDeserializer<CustomHostileData>, JsonSerializer<CustomHostileData> {
        @Override
        public CustomHostileData deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            int priority = GsonHelper.getAsInt(jObject, "priority");
            ObjTag<EntityType<?>> attacker = ObjTag.deserialize(jObject.get("attacker"), Registries.ENTITY_TYPE);
            ObjTag<EntityType<?>> target = ObjTag.deserialize(jObject.get("target"), Registries.ENTITY_TYPE);

            return new CustomHostileData(priority, attacker, target)
                    .chance(GsonHelper.getAsDouble(jObject, "chance", 1f))
                    .mustSee(GsonHelper.getAsBoolean(jObject, "requires_line_of_sight", true))
                    .addAttackGoal(GsonHelper.getAsBoolean(jObject, "add_attack_goal", false));
        }

        @Override
        public JsonElement serialize(CustomHostileData src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("priority", src.priority);
            jObject.add("attacker", context.serialize(src.attacker));
            jObject.add("target", context.serialize(src.target));
            if (src.chance < 1f)
                jObject.addProperty("chance", src.chance);
            if (!src.mustSee)
                jObject.addProperty("requires_line_of_sight", false);
            if (src.addAttackGoal)
                jObject.addProperty("add_attack_goal", true);
            return jObject;
        }
    }
}
