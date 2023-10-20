package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.shulker.ShulkerBullets;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {
	protected ShulkerBulletMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
		super(p_37248_, p_37249_);
	}

	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), index = 0)
	private MobEffectInstance onHitEntity(MobEffectInstance mobEffectInstance) {
		return ShulkerBullets.getLevitationInstance(this.level(), mobEffectInstance);
	}
}
