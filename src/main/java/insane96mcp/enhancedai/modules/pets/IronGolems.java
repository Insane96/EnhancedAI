package insane96mcp.enhancedai.modules.pets;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.PETS, description = "Use the `enhancedai:iron_golems/innate_resistance` and `enhancedai:iron_golems/fire_ticks_faster` entity type tag to add more iron golems.")
public class IronGolems extends Feature {
    public static final TagKey<EntityType<?>> INNATE_RESISTANCE = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("iron_golems/innate_resistance"));
    public static final TagKey<EntityType<?>> FIRE_TICKS_FASTER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("iron_golems/fire_ticks_faster"));

    @Config(min = 0d, max = 1d, description = "Resistance to damage taken by Iron Golems")
    public static Double innateResistance = 0.4d;
    @Config
    public static Boolean fireTicksFaster = true;

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!this.isEnabled()
                || innateResistance <= 0d
                || !(event.getEntity() instanceof IronGolem ironGolem)
                || !ironGolem.getType().is(INNATE_RESISTANCE))
            return;

        event.setAmount(event.getAmount() * innateResistance.floatValue());
    }

    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !fireTicksFaster
                || !(event.getEntity() instanceof IronGolem ironGolem)
                || !ironGolem.getType().is(FIRE_TICKS_FASTER))
            return;

        ironGolem.setRemainingFireTicks(Math.max(0, ironGolem.getRemainingFireTicks() - 3));
    }
}