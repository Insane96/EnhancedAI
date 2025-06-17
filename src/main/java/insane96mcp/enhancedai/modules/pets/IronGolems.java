package insane96mcp.enhancedai.modules.pets;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.PETS, description = "Use the `enhancedai:change_iron_golems` entity type tag to add more iron golems.")
public class IronGolems extends Feature {
    public static final TagKey<EntityType<?>> CHANGE_IRON_GOLEMS = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("change_iron_golems"));
    @Config(min = 0d, max = 1d, description = "Resistance to damage taken by Iron Golems")
    public static Double innateResistance = 0.4d;

    @Config
    public static Boolean fireTicksFaster = true;

    public IronGolems(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!this.isEnabled()
                || innateResistance <= 0d
                || !(event.getEntity() instanceof IronGolem ironGolem)
                || !ironGolem.getType().is(CHANGE_IRON_GOLEMS))
            return;

        event.setAmount(event.getAmount() * innateResistance.floatValue());
    }

    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !fireTicksFaster
                || !(event.getEntity() instanceof IronGolem ironGolem)
                || !ironGolem.getType().is(CHANGE_IRON_GOLEMS))
            return;

        ironGolem.setRemainingFireTicks(Math.max(0, ironGolem.getRemainingFireTicks() - 3));
    }
}