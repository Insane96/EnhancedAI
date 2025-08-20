package insane96mcp.enhancedai.modules.drowned;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@LoadFeature(module = Modules.Ids.DROWNED, description = "Makes drowned able to attack during daytime instead of standing still. Only entity types in the enhancedai:drowned/attack_during_day tag are affected by this feature.")
public class DrownedAttackDuringDay extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("drowned/attack_during_day"));

	public static boolean allowAttackDuringDay(Entity entity) {
		return Feature.isEnabled(DrownedAttackDuringDay.class) && entity.getType().is(AFFECTED_ENTITY_TYPES);
	}
}