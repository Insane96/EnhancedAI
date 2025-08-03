package insane96mcp.enhancedai.modules.pets;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.PETS, description = "Use the enhancedai:wolves/buffs and enhancedai:wolves/passive_healing entity type tags to add more wolves.")
public class Wolves extends Feature {
    public static final TagKey<EntityType<?>> BUFFS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("wolves/buffs"));
    public static final TagKey<EntityType<?>> PASSIVE_HEALING = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("wolves/passive_healing"));
    private static ResourceLocation BUFFS_PROCESSED;
    @Config
    public static Boolean doubleHpAndDamage = true;
    @Config
    public static Boolean bonusMovementSpeed = true;
    @Config(description = "Wolves will slowly heal like horses. This is 1 in x chance to heal 1 hp each tick.")
    public static Integer passiveHealSpeed = 900;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        BUFFS_PROCESSED = this.createDataKey("buffs_processed");
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !doubleHpAndDamage && !bonusMovementSpeed
                || !(event.getEntity() instanceof Wolf wolf)
                || !wolf.getType().is(BUFFS)
                || ModNBTData.get(wolf, BUFFS_PROCESSED, Boolean.class))
            return;

        if (bonusMovementSpeed)
            MCUtils.applyModifier(wolf, Attributes.MOVEMENT_SPEED, UUID.fromString("4be0baaf-17a5-4bad-af5a-1b1944ed0bf3"), "More Movement speed for Wolves", 0.25d, AttributeModifier.Operation.MULTIPLY_BASE, true);
        if (doubleHpAndDamage) {
            MCUtils.applyModifier(wolf, Attributes.MAX_HEALTH, UUID.fromString("f9353c93-25a5-42f4-a80e-4f4834b12e77"), "More HP for Wolves", 1d, AttributeModifier.Operation.MULTIPLY_BASE, true);
            MCUtils.applyModifier(wolf, Attributes.ATTACK_DAMAGE, UUID.fromString("e5e5bb8d-3eef-4e92-8897-909acdd4be61"), "More Damage for Wolves", 1d, AttributeModifier.Operation.MULTIPLY_BASE, true);
        }
        ModNBTData.put(wolf, BUFFS_PROCESSED, true);
    }

    @SubscribeEvent
    public void tryHealOnTick(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || passiveHealSpeed == 0
                || !(event.getEntity() instanceof Wolf wolf)
                || !wolf.getType().is(PASSIVE_HEALING))
            return;

        if (wolf.getHealth() < wolf.getMaxHealth() && wolf.getRandom().nextInt(passiveHealSpeed) == 0)
            wolf.heal(1);
    }
}