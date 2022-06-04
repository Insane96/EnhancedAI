package insane96mcp.enhancedai.modules.base.feature;

import insane96mcp.enhancedai.modules.base.ai.AvoidExplosionGoal;
import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.BlacklistConfig;
import insane96mcp.insanelib.util.IdTagMatcher;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Label(name = "Avoid Explosions", description = "Mobs will run away from exploding creepers / TNT")
public class AvoidExplosions extends Feature {

	private final BlacklistConfig entityBlacklistConfig;

	public ArrayList<IdTagMatcher> entityBlacklist;
	public boolean entityBlacklistAsWhitelist;

	public AvoidExplosions(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		entityBlacklistConfig = new BlacklistConfig(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Avoid Explosions AI", Collections.emptyList(), false);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.entityBlacklist = (ArrayList<IdTagMatcher>) IdTagMatcher.parseStringList(this.entityBlacklistConfig.listConfig.get());
		this.entityBlacklistAsWhitelist = this.entityBlacklistConfig.listAsWhitelistConfig.get();
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

		//Check for black/whitelist
		boolean isInWhitelist = false;
		boolean isInBlacklist = false;
		for (IdTagMatcher blacklistEntry : this.entityBlacklist) {
			if (blacklistEntry.matchesEntity(creatureEntity)) {
				if (!this.entityBlacklistAsWhitelist)
					isInBlacklist = true;
				else
					isInWhitelist = true;
				break;
			}
		}
		if (isInBlacklist || (!isInWhitelist && this.entityBlacklistAsWhitelist))
			return;

		creatureEntity.goalSelector.addGoal(1, new AvoidExplosionGoal(creatureEntity, 1.6d, 1.3d));
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
