package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.module.phantom.GrabbingPhantoms;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Phantom.class)
public abstract class PhantomMixin extends FlyingMob {
	protected PhantomMixin(EntityType<? extends FlyingMob> entityType, Level level) {
		super(entityType, level);
	}

	/// Grabbed entities hang below the phantom, with the top of the head touching the phantom's bottom.
	/// Eyes must stay outside the phantom's hitbox, otherwise the passenger would hit it whatever they look at.
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
		if (!GrabbingPhantoms.shouldHangBelow((Phantom) (Object) this))
			return super.getPassengerAttachmentPoint(entity, dimensions, partialTick);
		double y = entity.getVehicleAttachmentPoint(this).y - entity.getBbHeight();
		return new Vec3(0d, y, 0d);
	}

	@Override
	public boolean shouldRiderSit() {
		if (!GrabbingPhantoms.shouldHangBelow((Phantom) (Object) this))
			return super.shouldRiderSit();
		return false;
	}
}
