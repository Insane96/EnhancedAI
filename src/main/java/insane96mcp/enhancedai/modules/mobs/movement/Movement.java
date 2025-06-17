package insane96mcp.enhancedai.modules.mobs.movement;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes mobs be able to move in more ways, like climbing or swim. Only mobs in the enhancedai:allow_climbing entity type tag are allowed to climb.")
public class Movement extends Feature {
    public static final TagKey<EntityType<?>> ALLOW_CLIMBING = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("allow_climbing"));
    //public static final TagKey<EntityType<?>> ALLOW_TARGETING_LADDERS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "allow_climbing"));

    final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("6d2cb27e-e5e3-41b9-8108-f74131a90cce");
    final UUID UUID_MOV_SPEED_MULTIPLIER = UUID.fromString("8230810c-c178-4c01-b066-95e831b6defe");

    @Config(description = "If true, mobs will be able to climb (up and down)")
    public static Boolean allowClimbing = true;
    /*@Config
    @Label(name = "Target Ladders", description = "If true, mobs try to find climbable blocks to reach the target")
    public static Boolean targetLadders = false;*/
    @Config(min = 0d, max = 8d, description = "Use Mobs Properties Randomness to have more control over this.")
    public static Double bonusMovementSpeed = 0.15d;

    @Config(min = 0d, max = 4d, description = "How faster mobs can swim. Setting to 0 will leave the swim speed as vanilla. I recommend using mods like Mobs Properties Randomness to have more control over the attribute.")
    public static Double swimSpeedAdditionMultiplier = 2.5d;

    public Movement(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(ALLOW_CLIMBING))
            return;

        if (allowClimbing) {
            mob.goalSelector.addGoal(3, new ClimbLaddersGoal(mob));
        }

        if (bonusMovementSpeed > 0d)
            MCUtils.applyModifier(mob, Attributes.MOVEMENT_SPEED, UUID_MOV_SPEED_MULTIPLIER, "Enhanced AI Mov Speed Bonus", bonusMovementSpeed, AttributeModifier.Operation.MULTIPLY_BASE, true);

        /*if (targetLadders) {
            mob.targetSelector.addGoal(0, new FindLaddersGoal(mob));
        }*/

        if (swimSpeedAdditionMultiplier != 0d) {
            MCUtils.applyModifier(mob, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Swim Speed Multiplier", swimSpeedAdditionMultiplier, AttributeModifier.Operation.MULTIPLY_BASE, false);
        }
    }
}