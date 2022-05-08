package insane96mcp.enhancedai.setup;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EAAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, EnhancedAI.MOD_ID);

	public static final RegistryObject<Attribute> XRAY_FOLLOW_RANGE = ATTRIBUTES.register("generic.xray_follow_range", () -> new RangedAttribute("attribute.name.generic.xray_follow_range", 32d, 0d, 256d));
}
