package insane96mcp.enhancedai.modules.mobs.anticheese;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;

import java.util.EnumSet;

public class BreakVehicleGoal extends Goal {
	protected final Mob mob;
	protected Entity vehicle;
	int attackCooldown;

	public BreakVehicleGoal(Mob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE));
	}

	public boolean canUse() {
		if (hasValidVehicle()) {
			this.vehicle = this.mob.getVehicle();
			return true;
		}
		return false;
	}

	public boolean canContinueToUse() {
		return hasValidVehicle();
	}

	private boolean hasValidVehicle() {
		return this.mob.getVehicle() != null && this.mob.getVehicle().getType().is(AntiCheese.ANTI_CHEESE_VEHICLES);
	}

	public void start() {
		this.attackCooldown = reducedTickDelay(15);
	}

	public void stop() {
		this.vehicle = null;
	}

	public void tick() {
		this.attackCooldown--;
		if (this.attackCooldown > 0)
			return;
		if (this.mob instanceof Creeper creeper) {
			creeper.ignite();
		}
		else {
			this.vehicle.playSound(SoundEvents.PLAYER_ATTACK_WEAK);
			this.mob.playAmbientSound();
			this.mob.swing(InteractionHand.MAIN_HAND);
			this.mob.doHurtTarget(this.vehicle);
		}
		this.attackCooldown = reducedTickDelay(15);
	}
}