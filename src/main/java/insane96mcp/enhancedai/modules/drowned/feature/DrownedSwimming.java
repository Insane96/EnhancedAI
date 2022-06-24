package insane96mcp.enhancedai.modules.drowned.feature;

import insane96mcp.enhancedai.setup.Config;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.config.Blacklist;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.UUID;

@Label(name = "Drowned Swimming", description = "Makes drowned swim speed based off swim speed attribute instead of movement speed.")
public class DrownedSwimming extends Feature {

	private final ForgeConfigSpec.DoubleValue swimSpeedMultiplierConfig;
	private final Blacklist.Config entityBlacklistConfig;

	public double swimSpeedMultiplier = 0.25d;
	public Blacklist entityBlacklist;

	public DrownedSwimming(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		swimSpeedMultiplierConfig = Config.builder
				.comment("Multiplier for the swim speed of Drowneds. Note that the swim speed is also affected by the Movement Feature. Set to 0 to disable the multiplier.")
				.defineInRange("Swim Speed Multiplier", this.swimSpeedMultiplier, 0d, 4d);

		entityBlacklistConfig = new Blacklist.Config(Config.builder, "Entity Blacklist", "Entities that shouldn't get the Swim Control from drowneds")
				.setDefaultList(Collections.emptyList())
				.setIsDefaultWhitelist(false)
				.build();
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.swimSpeedMultiplier = this.swimSpeedMultiplierConfig.get();
		this.entityBlacklist = this.entityBlacklistConfig.get();
	}

	final UUID UUID_SWIM_SPEED_MULTIPLIER = UUID.fromString("ba2adf05-2438-4d1f-8165-89173f0a1eae");

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Drowned drowned)
				|| this.entityBlacklist.isEntityBlackOrNotWhitelist(drowned))
			return;

		drowned.moveControl = new EADrownedMoveControl(drowned);

		if (this.swimSpeedMultiplier > 0d) {
			MCUtils.applyModifier(drowned, ForgeMod.SWIM_SPEED.get(), UUID_SWIM_SPEED_MULTIPLIER, "Enhanced AI Drowneds Swim Speed Multiplier", this.swimSpeedMultiplier - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
		}
	}

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
				float f2 = Mth.lerp(0.125F, this.drowned.getSpeed(), f1);
				this.drowned.setSpeed(f2);
				this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add((double)f2 * d0 * 0.005D, (double)f2 * d1 * 0.1D, (double)f2 * d2 * 0.005D));
			} else {
				if (!this.drowned.isOnGround()) {
					this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
				}

				super.tick();
			}

		}
	}
}
