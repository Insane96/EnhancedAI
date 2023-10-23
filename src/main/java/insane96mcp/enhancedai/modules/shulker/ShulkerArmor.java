package insane96mcp.enhancedai.modules.shulker;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.UUID;

@Label(name = "Shulker Armor", description = "Use the enhancedai:apply_shulker_armor_modifiers to add more shulkers that are affected by this feature.")
@LoadFeature(module = Modules.Ids.SHULKER)
public class ShulkerArmor extends Feature {
    public static final TagKey<EntityType<?>> APPLY_ARMOR_MODIFIERS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "apply_shulker_armor_modifiers"));
    @Config(min = 1, max = 40)
    @Label(name = "Armor when closed")
    public static Double armorWhenClosed = 30d;
    @Config(min = 1, max = 40)
    @Label(name = "Armor when peeking")
    public static Double armorWhenPeeking = 20d;
    @Config(min = 1, max = 40)
    @Label(name = "Armor when open")
    public static Double armorWhenOpen = 10d;

    private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
    public static AttributeModifier CLOSED_MODIFIER;
    public static AttributeModifier PEEK_MODIFIER;
    public static AttributeModifier OPEN_MODIFIER;

    public ShulkerArmor(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        CLOSED_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", armorWhenClosed, AttributeModifier.Operation.ADDITION);
        PEEK_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", armorWhenPeeking, AttributeModifier.Operation.ADDITION);
        OPEN_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", armorWhenOpen, AttributeModifier.Operation.ADDITION);
    }

    public static boolean isAffectedByArmorModifers(Shulker shulker) {
        return Feature.isEnabled(ShulkerArmor.class) && shulker.getType().is(APPLY_ARMOR_MODIFIERS);
    }
}