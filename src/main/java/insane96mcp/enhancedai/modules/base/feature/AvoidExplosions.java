package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.AvoidExplosionGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.List;

@Label(name = "Avoid Explosions", description = "Mobs will run away from exploding creepers / TNT")
public class AvoidExplosions extends Feature {

	private final ForgeConfigSpec.DoubleValue runSpeedNearConfig;
	private final ForgeConfigSpec.DoubleValue runSpeedFarConfig;
	private final Blacklist.Config entityBlacklistConfig;

	public double runSpeedNear = 1.4d;
	public double runSpeedFar = 1.2d;
	public Blacklist entityBlacklist;

	public AvoidExplosions(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		runSpeedNearConfig = Config.builder
				.comment("Speed multiplier when the mob runs from explosions and it's within 7 blocks from him.")
				.defineInRange("Flee speed Multiplier Near", this.runSpeedNear, 0, 10d);
		runSpeedFarConfig = Config.builder
				.comment("Speed multiplier when the mob runs from explosions and it's farther than 7 blocks from him.")
				.defineInRange("Flee speed Multiplier Far", this.runSpeedFar, 0, 10d);
		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Avoid Explosions AI")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.runSpeedNear = this.runSpeedNearConfig.get();
		this.runSpeedFar = this.runSpeedFarConfig.get();
		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled())
			return;

		addAvoidAI(event);
		alertTNT(event);
	}

	private void addAvoidAI(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof PathfinderMob creatureEntity))
			return;

		if (this.entityBlacklist.isEntityBlackOrNotWhitelist(creatureEntity))
			return;

		creatureEntity.goalSelector.addGoal(1, new AvoidExplosionGoal(creatureEntity, this.runSpeedNear, this.runSpeedFar));
	}

	private void alertTNT(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof PrimedTnt tnt))
			return;

		List<PathfinderMob> creaturesNearby = tnt.level.getEntitiesOfClass(PathfinderMob.class, tnt.getBoundingBox().inflate(8d));
		for (PathfinderMob creatureEntity : creaturesNearby) {
			creatureEntity.goalSelector.availableGoals.forEach(prioritizedGoal -> {
				if (prioritizedGoal.getGoal() instanceof AvoidExplosionGoal avoidExplosionGoal) {
					avoidExplosionGoal.run(tnt, 8d);
				}
			});
		}
	}
}
