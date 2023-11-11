package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.mobs.OpenDoors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vindicator.class)
public abstract class VindicatorMixin extends AbstractIllager {
	protected VindicatorMixin(EntityType<? extends AbstractIllager> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/ai/navigation/GroundPathNavigation;setCanOpenDoors(Z)V"), method = "customServerAiStep")
	public void onSetCanOpenDoors(CallbackInfo ci) {
		if (OpenDoors.shouldBeAbleToOpenDoors((Vindicator) (Object) this))
			((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
	}
}
