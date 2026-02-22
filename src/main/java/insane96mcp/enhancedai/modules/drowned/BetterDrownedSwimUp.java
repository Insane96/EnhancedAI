package insane96mcp.enhancedai.modules.drowned;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.Ids.DROWNED, description = "Replaces the drowned swim up goal with a better one, allowing them to leap out of the water. Only entity types in the enhancedai:drowned/change_swim_up tag are affected by this feature.")
public class BetterDrownedSwimUp extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("drowned/change_swim_up"));

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Drowned drowned)
				|| !drowned.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		drowned.goalSelector.removeAllGoals(goal -> goal instanceof Drowned.DrownedSwimUpGoal);
		drowned.goalSelector.addGoal(6, new DrownedSwimUpGoal(drowned, 1.0D, drowned.level().getSeaLevel()));
	}

	static class DrownedSwimUpGoal extends Goal {
		private final Drowned drowned;
		private final double speedModifier;
		private final int seaLevel;
		private boolean stuck;

		private int leapTick = 0;

		public DrownedSwimUpGoal(Drowned pDrowned, double pSpeedModifier, int pSeaLevel) {
			this.drowned = pDrowned;
			this.speedModifier = pSpeedModifier;
			this.seaLevel = pSeaLevel;
		}

		public boolean canUse() {
			return this.drowned.isInWater() && this.drowned.getY() < (double)(this.seaLevel);
		}

		public boolean canContinueToUse() {
			return this.canUse() && !this.stuck && this.leapTick > 0;
		}

		public void tick() {
			if (this.drowned.getY() < (double)(this.seaLevel - 1) && (this.drowned.getNavigation().isDone() || this.closeToNextPos())) {
				Vec3 vec3 = DefaultRandomPos.getPosTowards(this.drowned, 4, 8, new Vec3(this.drowned.getX(), (this.seaLevel + 1), this.drowned.getZ()), ((float)Math.PI / 2F));
				if (vec3 == null) {
					this.stuck = true;
					return;
				}

				this.drowned.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
			}
			if (--leapTick <= 0 && this.drowned.getY() >= (double)(this.seaLevel - 1)) {
				LivingEntity target = this.drowned.getTarget();
				if (target != null && !target.isInWater() && target.onGround()) {
					Vec3 dir = new Vec3(
							target.getX() - this.drowned.getX(),
							target.getY() - this.drowned.getY(),
							target.getZ() - this.drowned.getZ()
					).normalize();

					this.drowned.setDeltaMovement(new Vec3(
							dir.x * 0.5,
							0.4,
							dir.z * 0.5
					));

					this.drowned.getNavigation().stop();
				}
			}

		}

		public void start() {
			this.drowned.setSearchingForLand(true);
			this.stuck = false;
		}

		public void stop() {
			this.drowned.setSearchingForLand(false);
			this.leapTick = this.adjustedTickDelay(10);
		}

		protected boolean closeToNextPos() {
			Path path = this.drowned.getNavigation().getPath();
            if (path == null)
                return false;

            BlockPos blockpos = path.getTarget();
            double d0 = this.drowned.distanceToSqr(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            return d0 < 4.0D;
        }
	}
}
