package insane96mcp.enhancedai.module.drowned;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
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
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.EnumSet;

@LoadFeature(module = EAIModules.DROWNED, description = "Replaces the drowned swim up goal with a better one, allowing them to leap out of the water. Only entity types in the enhancedai:drowned/change_swim_up tag are affected by this feature.")
public class BetterDrownedSwimUp extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("drowned/change_swim_up"));

	// Number of ticks the leap's push (towards the target) keeps being re-applied for, instead of just once.
	// This lets the drowned climb over short ledges/walls: it keeps thrusting up (fighting gravity) while also
	// being pushed sideways, so by the time it's above an obstacle the horizontal push carries it over instead
	// of just slamming it into the wall's face a single time.
	private static final int LEAP_PUSH_DURATION = 4;

	public static EAIData<Integer> LEAP_PUSH_TICKS;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		LEAP_PUSH_TICKS = EAIData.ofInt(this.createDataKey("leap_push_ticks"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| !(event.getEntity() instanceof Drowned drowned)
				|| !drowned.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		drowned.goalSelector.removeAllGoals(goal -> goal instanceof Drowned.DrownedSwimUpGoal);
		drowned.goalSelector.removeAllGoals(goal -> goal instanceof Drowned.DrownedGoToWaterGoal);
		drowned.goalSelector.addGoal(1, new DrownedSwimUpGoal(drowned, 1.0D, drowned.level().getSeaLevel()));
	}

	@SubscribeEvent
	public void onTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Drowned drowned)
				|| drowned.level().isClientSide
				|| !drowned.getType().is(AFFECTED_ENTITY_TYPES))
			return;

		// Runs independently of DrownedSwimUpGoal's state: by the time this fires, the goal may have already
		// stopped (e.g. because the drowned is no longer in water), but we still need to keep pushing it.
		int pushTicks = LEAP_PUSH_TICKS.get(drowned);
		if (pushTicks <= 0)
			return;

		LivingEntity target = drowned.getTarget();
		if (target == null || target.isInWater()) {
			LEAP_PUSH_TICKS.apply(drowned, 0);
			return;
		}

		Vec3 dir = new Vec3(
				target.getX() - drowned.getX(),
				target.getY() - drowned.getY(),
				target.getZ() - drowned.getZ()
		).normalize();

		drowned.setDeltaMovement(dir.x * 0.3, 0.3, dir.z * 0.3);
		LEAP_PUSH_TICKS.apply(drowned, pushTicks - 1);
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
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		public boolean canUse() {
			// Keep the drowned in "swim" navigation for as long as it's physically in water, regardless of
			// whether this goal actually takes over movement below.
			this.drowned.setSearchingForLand(this.drowned.isInWater());

			if (!this.drowned.isInWater() || this.drowned.getY() >= (double)(this.seaLevel)) {
				//EnhancedAI.LOGGER.debug("canUse(): {}, !this.drowned.isInWater() {}, this.drowned.getY() >= (double)(this.seaLevel) {},", this.drowned, !this.drowned.isInWater(), this.drowned.getY() >= (double)(this.seaLevel));
				return false;
			}

			LivingEntity target = this.drowned.getTarget();
			if (target == null) {
				// No target: fall back to the original "search for land" behaviour over the whole water column,
				// DrownedAttackGoal won't be competing for movement since it needs a target to run at all.
				//EnhancedAI.LOGGER.debug("canUse(): {}, No target", this.drowned);
				return true;
			}

			// Has a target: let DrownedAttackGoal handle the actual chase/approach, we only step in right
			// near the surface when there's an actual reason to leap out (a target on land).
			//EnhancedAI.LOGGER.debug("canUse(): {}, this.drowned.getY() >= this.seaLevel - 2d {}, !target.isInWater() {},", this.drowned, this.drowned.getY() >= this.seaLevel - 2d, !target.isInWater());
			return this.drowned.getY() >= this.seaLevel - 2d && !target.isInWater();
		}

		public boolean canContinueToUse() {
			//EnhancedAI.LOGGER.debug("canContinueToUse(): {}, !this.stuck {}, this.leapTick > 0 {},", this.drowned, !this.stuck, this.leapTick > 0);
			return this.canUse() && !this.stuck && this.leapTick > 0;
		}

		public void tick() {
			//EnhancedAI.LOGGER.debug("tick(): {}, this.leapTick {}, this.drowned.getY() >= (double)(this.seaLevel - 2d) {},", this.drowned, this.leapTick, this.drowned.getY() >= this.seaLevel - 2d);
			if (this.drowned.getTarget() != null)
				this.drowned.getLookControl().setLookAt(this.drowned.getTarget());
			if (this.drowned.getTarget() == null && this.drowned.getY() < (double)(this.seaLevel) && (this.drowned.getNavigation().isDone() || this.closeToNextPos())) {
				Vec3 vec3 = DefaultRandomPos.getPosTowards(this.drowned, 4, 8, new Vec3(this.drowned.getX(), this.seaLevel, this.drowned.getZ()), ((float)Math.PI / 2F));
				if (vec3 == null) {
					this.stuck = true;
					//EnhancedAI.LOGGER.debug("tick(): {}, stuck", this.drowned);
					return;
				}

				//EnhancedAI.LOGGER.debug("tick(): {}, move to {},", this.drowned, vec3);
				this.drowned.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
			}
			if (--leapTick <= 0 && this.drowned.getY() >= this.seaLevel - 2d) {
				LivingEntity target = this.drowned.getTarget();
				if (target != null && !target.isInWater()) {
					BetterDrownedSwimUp.LEAP_PUSH_TICKS.apply(this.drowned, BetterDrownedSwimUp.LEAP_PUSH_DURATION);
					this.drowned.getNavigation().stop();
				}
			}

		}

		public void start() {
			this.stuck = false;
			//EnhancedAI.LOGGER.debug("start(): {},", this.drowned);
		}

		public void stop() {
			//EnhancedAI.LOGGER.debug("stop(): {},", this.drowned);
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
