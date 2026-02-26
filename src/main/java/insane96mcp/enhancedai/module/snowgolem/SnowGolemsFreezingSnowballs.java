package insane96mcp.enhancedai.module.snowgolem;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@LoadFeature(module = EAIModules.SNOW_GOLEM, description = "Snow golems snowballs freeze entities hit. Only entity types in enhancedai:snow_golem/freezing_snowballs tag are affected by this feature.")
public class SnowGolemsFreezingSnowballs extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("snow_golem/freezing_snowballs"));
    @Config(min = 0, description = "How many ticks the entity hit is frozen for.")
    public static Integer ticks = 30;
    @Config
    public static Boolean stackFreeze = true;

	public static EAIData<Integer> TICKS;
	public static EAIData<Boolean> STACK;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		TICKS = EAIData.ofInt(this.createDataKey("ticks"));
		STACK = EAIData.ofBool(this.createDataKey("stack"));
    }

    @SubscribeEvent
    public void onProjectileImpactEvent(ProjectileImpactEvent event) {
        if (!this.isEnabled()
                || !(event.getProjectile().getOwner() instanceof SnowGolem snowGolem)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)
                || !(entityHitResult.getEntity() instanceof LivingEntity entityHit)
                || entityHitResult.getEntity() instanceof SnowGolem)
            return;

		if (!TICKS.has(snowGolem))
			return;

		int ticks = TICKS.get(snowGolem);
		if (ticks <= 0)
			return;

		int ticksFrozen = ticks;
		if (STACK.get(snowGolem))
			ticksFrozen += entityHit.getTicksFrozen();
		entityHit.setTicksFrozen(ticksFrozen);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof SnowGolem snowGolem)
				|| !snowGolem.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		TICKS.applyIfAbsent(snowGolem, ticks);
		STACK.applyIfAbsent(snowGolem, stackFreeze);
    }
}