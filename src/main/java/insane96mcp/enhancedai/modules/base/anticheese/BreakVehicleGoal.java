package insane96mcp.enhancedai.modules.base.anticheese;

import insane96mcp.enhancedai.EnhancedAI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;

import java.util.EnumSet;

public class BreakVehicleGoal extends Goal {
	public static final TagKey<EntityType<?>> VEHICLES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "vehicles"));
	protected final Mob mob;
	protected Entity veichle;
	int attackCooldown;

	public BreakVehicleGoal(Mob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE));
	}

	public boolean canUse() {
		if (hasValidVehicle()) {
			this.veichle = this.mob.getVehicle();
			return true;
		}
		return false;
	}

	public boolean canContinueToUse() {
		return hasValidVehicle();
	}

	private boolean hasValidVehicle() {
		return this.mob.getVehicle() != null && this.mob.getVehicle().getType().is(VEHICLES);
	}

	public void start() {
		this.attackCooldown = reducedTickDelay(15);
	}

	public void stop() {
		this.veichle = null;
	}

	public void tick() {
		this.attackCooldown--;
		if (this.attackCooldown > 0)
			return;
		if (this.mob instanceof Creeper creeper) {
			creeper.ignite();
		}
		else {
			this.veichle.playSound(SoundEvents.PLAYER_ATTACK_WEAK);
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(this.veichle);
		}
		this.attackCooldown = reducedTickDelay(15);
	}
}