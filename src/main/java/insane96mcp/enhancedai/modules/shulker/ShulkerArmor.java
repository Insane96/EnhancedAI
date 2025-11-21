package insane96mcp.enhancedai.modules.shulker;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.SHULKER, description = "Only entity types in the `enhancedai:shulker/apply_armor_modifiers` tag will be affected by this feature.")
public class ShulkerArmor extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("shulker/apply_armor_modifiers"));
    @Config(min = 0)
    public static Double armorWhenClosed = 24d;
    @Config(min = 0)
    public static Double armorWhenPeeking = 16d;
    @Config(min = 0)
    public static Double armorWhenOpen = 8d;

    private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
    public static AttributeModifier CLOSED_MODIFIER;
    public static AttributeModifier PEEK_MODIFIER;
    public static AttributeModifier OPEN_MODIFIER;

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        CLOSED_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", armorWhenClosed, AttributeModifier.Operation.ADDITION);
        PEEK_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", armorWhenPeeking, AttributeModifier.Operation.ADDITION);
        OPEN_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", armorWhenOpen, AttributeModifier.Operation.ADDITION);
    }

    public static boolean isAffectedByArmorModifiers(Shulker shulker) {
        return Feature.isEnabled(ShulkerArmor.class) && shulker.getType().is(AFFECTED_ENTITY_TYPES) && !Spawning.isUnaffectedByFeatures(shulker);
    }
}