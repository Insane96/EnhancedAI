package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.ai.EAAvoidEntityGoal;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.targeting.EANearestAttackableTarget;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Warden Interacting", description = "Mobs can flee or attack Wardens. Use the entity type tag enhancedai:ignore_warden_interaction to blacklist mobs.")
@LoadFeature(module = Modules.Ids.MOBS)
public class WardenInteractions extends Feature {
	public static final TagKey<EntityType<?>> IGNORE_WARDEN_INTERACTION = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "ignore_warden_interaction"));
	@Config
	@Label(name = "Mobs flee from the Warden")
	public static Boolean flee = true;
	@Config(min = 0d, max = 10d)
	@Label(name = "Flee speed Multiplier Near", description = "Speed multiplier when the mob runs from explosions and it's within 7 blocks from him.")
	public static Double runSpeedNear = 1.25d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Flee speed Multiplier Far", description = "Speed multiplier when the mob runs from explosions and it's farther than 7 blocks from him.")
	public static Double runSpeedFar = 1.1d;
	@Config
	@Label(name = "Mobs target the Warden")
	public static Boolean target = false;

	public WardenInteractions(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof PathfinderMob entity)
				|| entity.getType().is(IGNORE_WARDEN_INTERACTION))
			return;
		if (target) {
			entity.targetSelector.addGoal(2, new EANearestAttackableTarget<>(entity, Warden.class, false, false, TargetingConditions.forCombat()));
		}
		else if (flee) {
			EAAvoidEntityGoal<Warden> avoidEntityGoal = new EAAvoidEntityGoal<>(entity, Warden.class, (float) 12, (float) 7, runSpeedNear, runSpeedFar);
			entity.goalSelector.addGoal(1, avoidEntityGoal);
		}
	}
}
