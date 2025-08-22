package insane96mcp.enhancedai.modules.snowgolem;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.SNOW_GOLEM, description = "Increase stats of snow golems. Only entity types in enhancedai:snow_golem/bonus_stats tag are affected by this feature.")
public class SnowGolemsBonusStats extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golem/bonus_stats"));
    @Config(min = 0)
    public static Integer bonusArmor = 5;

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof SnowGolem snowGolem)
				|| !snowGolem.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        MCUtils.applyModifier(snowGolem, Attributes.ARMOR, UUID.fromString("4be0baaf-17a5-4bad-af5a-1b1944ed0bf3"), "Snow golems bonus stats", bonusArmor, AttributeModifier.Operation.ADDITION, true);
    }
}