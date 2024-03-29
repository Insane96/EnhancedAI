package insane96mcp.enhancedai.modules.drowned;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Drowned Swimming", description = "Makes drowned swim speed based off swim speed attribute instead of movement speed. Only drowneds in the enhancedai:change_drowned_swimming entity type tag are affected by this feature.")
@LoadFeature(module = Modules.Ids.DROWNED)
public class DrownedSwimming extends Feature {
	public static final TagKey<EntityType<?>> CHANGE_DROWNED_SWIMMING = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "change_drowned_swimming"));

	final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("ba2adf05-2438-4d1f-8165-89173f0a1eae");

	@Config(min = 0d, max = 4d)
	@Label(name = "Swim Speed Multiplier", description = "Multiplier for the swim speed of Drowneds. Note that the swim speed is also affected by the Movement Feature. Set to 0 to disable the multiplier.")
	public static Double swimSpeedMultiplier = 0.3d;

	public DrownedSwimming(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Drowned drowned)
				|| !drowned.getType().is(CHANGE_DROWNED_SWIMMING))
			return;

		drowned.moveControl = new EADrownedMoveControl(drowned);
		//drowned.waterNavigation.getNodeEvaluator().setCanFloat(true);
		((SwimNodeEvaluator) drowned.waterNavigation.getNodeEvaluator()).allowBreaching = true;
		drowned.goalSelector.removeAllGoals(goal -> goal instanceof Drowned.DrownedSwimUpGoal);
		drowned.goalSelector.addGoal(6, new DrownedSwimUpGoal(drowned, 1.0D, drowned.level().getSeaLevel()));;

		if (swimSpeedMultiplier > 0d) {
			MCUtils.applyModifier(drowned, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Drowneds Swim Speed Multiplier", swimSpeedMultiplier - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
		}
	}

	//Same as MoveControl but uses forge:swim_speed instead of minecraft:generic.movement_speed
	static class EADrownedMoveControl extends MoveControl {
		private final Drowned drowned;

		public EADrownedMoveControl(Drowned p_32433_) {
			super(p_32433_);
			this.drowned = p_32433_;
		}

		public void tick() {
			LivingEntity livingentity = this.drowned.getTarget();
			if (this.drowned.wantsToSwim() && this.drowned.isInWater()) {
				if (livingentity != null && livingentity.getY() > this.drowned.getY() || this.drowned.searchingForLand) {
					this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
				}

				if (this.operation != MoveControl.Operation.MOVE_TO || this.drowned.getNavigation().isDone()) {
					this.drowned.setSpeed(0.0F);
					return;
				}

				double d0 = this.wantedX - this.drowned.getX();
				double d1 = this.wantedY - this.drowned.getY();
				double d2 = this.wantedZ - this.drowned.getZ();
				double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
				d1 /= d3;
				float f = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
				this.drowned.setYRot(this.rotlerp(this.drowned.getYRot(), f, 90.0F));
				this.drowned.yBodyRot = this.drowned.getYRot();
				float f1 = (float)(this.speedModifier * this.drowned.getAttributeValue(ForgeMod.SWIM_SPEED.get()));
				float f2 = Mth.lerp(1F, this.drowned.getSpeed(), f1);
				this.drowned.setSpeed(f2);
				this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add((double)f2 * d0 * 0.005D, (double)f2 * d1 * 0.05D, (double)f2 * d2 * 0.005D));
				this.drowned.setPose(Pose.SWIMMING);
			}
			else {
				if (!this.drowned.onGround()) {
					this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
				}
				/*else {
					this.drowned.getNavigation().stop();
				}*/
				this.drowned.setPose(Pose.STANDING);

				super.tick();
			}

		}
	}

	static class DrownedSwimUpGoal extends Goal {
		private final Drowned drowned;
		private final double speedModifier;
		private final int seaLevel;
		private boolean stuck;

		public DrownedSwimUpGoal(Drowned pDrowned, double pSpeedModifier, int pSeaLevel) {
			this.drowned = pDrowned;
			this.speedModifier = pSpeedModifier;
			this.seaLevel = pSeaLevel;
		}

		public boolean canUse() {
			return !this.drowned.level().isDay() && this.drowned.isInWater() && this.drowned.getY() < (double)(this.seaLevel);
		}

		public boolean canContinueToUse() {
			return this.canUse() && !this.stuck;
		}

		public void tick() {
			if (this.drowned.getY() < (double)(this.seaLevel + 1) && (this.drowned.getNavigation().isDone() || this.closeToNextPos())) {
				Vec3 vec3 = DefaultRandomPos.getPosTowards(this.drowned, 4, 8, new Vec3(this.drowned.getX(), (this.seaLevel + 1), this.drowned.getZ()), ((float)Math.PI / 2F));
				if (vec3 == null) {
					this.stuck = true;
					return;
				}

				this.drowned.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
			}

		}

		public void start() {
			this.drowned.setSearchingForLand(true);
			this.stuck = false;
		}

		public void stop() {
			this.drowned.setSearchingForLand(false);
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
