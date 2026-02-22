package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Only entity types in the enhancedai:mobs/air_stealer tag are affected by this feature.")
public class AirSteal extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/air_stealer"));

    @Config(min = 0, max = 128, description = "How many ticks of air are stolen from entities when attacked by the mob.")
    public static Integer stolenTicks = 40;

    public static EAIData<Integer> STOLEN_TICKS;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        STOLEN_TICKS = EAIData.ofInt(this.createDataKey("stolen_ticks"));
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        STOLEN_TICKS.applyIfAbsent(mob, stolenTicks);
    }

    @SubscribeEvent
    public void onAttack(LivingHurtEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)
                || STOLEN_TICKS.get(attacker) <= 0)
            return;

        event.getEntity().setAirSupply(event.getEntity().getAirSupply() - STOLEN_TICKS.get(attacker));
    }
}
