package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes drowned swim speed based off swim speed attribute instead of movement speed.")
public class Swimmers extends Feature {
    public static final TagKey<EntityType<?>> SWIM_SPEED_MULTIPLIER_AFFECTED = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/swim_speed_multiplier"));
	final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("ba2adf05-2438-4d1f-8165-89173f0a1eae");

	@Config(min = 0d, max = 4d, description = "Multiplier for the swim speed of drowned. Note that the swim speed is also changed by the MPR Data Pack feature. Set to 0 to disable the multiplier.")
	public static Double drowned$swimSpeedMultiplier = 0.3d;
    @Config
    public static Boolean drowned$breaching = true;

    @Config(description = "Vanilla is 0.01")
    public static Double fishSwimSpeed = 0.025d;

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity()))
			return;

        if (drowned$swimSpeedMultiplier > 0d && event.getEntity() instanceof LivingEntity livingEntity && livingEntity.getType().is(SWIM_SPEED_MULTIPLIER_AFFECTED))
            MCUtils.applyModifier(livingEntity, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Drowned Swim Speed Multiplier", drowned$swimSpeedMultiplier - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (drowned$breaching && event.getEntity() instanceof Drowned drowned)
		    ((SwimNodeEvaluator) drowned.waterNavigation.getNodeEvaluator()).allowBreaching = true;
	}

    public static float fishSwimSpeed(float original) {
        return !Feature.isEnabled(Swimmers.class) ? original : fishSwimSpeed.floatValue();
    }
}
