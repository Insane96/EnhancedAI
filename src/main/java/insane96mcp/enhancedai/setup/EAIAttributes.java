package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EAIAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, EnhancedAI.MOD_ID);
	public static final DeferredHolder<Attribute, Attribute> SPAWN_REINFORCEMENTS_CHANCE = ATTRIBUTES.register("spawn_reinforcements_chance", () -> new RangedAttribute("attribute.name.spawn_reinforcements_chance", 0d, 0d, Double.MAX_VALUE));

	public static final DeferredHolder<Attribute, Attribute> XRAY_FOLLOW_RANGE = ATTRIBUTES.register("xray_follow_range", () -> new RangedAttribute("attribute.name.xray_follow_range", 0d, 0d, 256d));
}
