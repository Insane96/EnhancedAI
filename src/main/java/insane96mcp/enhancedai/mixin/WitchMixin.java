package insane96mcp.enhancedai.mixin;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.witch.feature.ThirstyWitches;
import insane96mcp.enhancedai.modules.witch.feature.WitchPotionThrowing;
import insane96mcp.enhancedai.setup.EAStrings;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mixin(Witch.class)
public abstract class WitchMixin extends Raider {
	private static final UUID SPEED_MODIFIER_DRINKING_UUID = UUID.fromString("9629aa37-c8a0-4ef9-a99a-7ec039a5a4dd");
	private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(SPEED_MODIFIER_DRINKING_UUID, "Drinking speed penalty", -0.25D, AttributeModifier.Operation.MULTIPLY_BASE);

	@Shadow
	private NearestHealableRaiderTargetGoal<Raider> healRaidersGoal;

	@Shadow
	private NearestAttackableWitchTargetGoal<Player> attackPlayersGoal;

	@Shadow
	private int usingTime;

	private int invisibilityCooldown = 20;

	protected WitchMixin(EntityType<? extends Raider> p_37839_, Level p_37840_) {
		super(p_37839_, p_37840_);
	}

	@ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;addTransientModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
	public AttributeModifier onAddSpeedPenalty(AttributeModifier attributeModifier) {
		return SPEED_MODIFIER_DRINKING;
	}
	@ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;removeModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V"))
	public AttributeModifier onRemoveSpeedPenalty(AttributeModifier attributeModifier) {
		return SPEED_MODIFIER_DRINKING;
	}

	@Inject(at = @At("HEAD"), method = "aiStep", cancellable = true)
	private void aiStep(CallbackInfo ci) {
		if (Modules.witch.isEnabled())
			ci.cancel();
		else
			return;

		if (this.level().isClientSide
				|| !this.isAlive()
				|| this.getPersistentData().getBoolean(EAStrings.Tags.Witch.PERFORMING_DARK_ARTS)) {
			super.aiStep();
			return;
		}

		this.healRaidersGoal.decrementCooldown();
		this.attackPlayersGoal.setCanAttack(this.healRaidersGoal.getCooldown() <= 0);

		Collection<MobEffectInstance> mobEffectInstances = new ArrayList<>();

		if (this.isDrinkingPotion()) {
			if (this.usingTime % 8 == 0)
				this.playSound(SoundEvents.GENERIC_DRINK, 1.0f, this.random.nextFloat() * 0.1F + 0.9F);
			if (this.usingTime-- <= 0) {
				this.setUsingItem(false);
				ItemStack itemstack = this.getMainHandItem();
				this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
				if (itemstack.is(Items.POTION)) {
					List<MobEffectInstance> list = PotionUtils.getMobEffects(itemstack);
					for(MobEffectInstance mobeffectinstance : list) {
						this.addEffect(new MobEffectInstance(mobeffectinstance));
					}
				}
				else if (itemstack.is(Items.MILK_BUCKET)) {
					this.curePotionEffects(itemstack);
				}

				this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
			}
		}
		else {
			if (this.getTarget() != null && this.getTarget() instanceof Player && this.distanceToSqr(this.getTarget()) > 64d) {
				List<MobEffectInstance> listToLoop = ThirstyWitches.drinkPotion;
				for (MobEffectInstance mobEffectInstance : listToLoop) {
					if (this.hasEffect(mobEffectInstance.getEffect()))
						continue;

					mobEffectInstances.add(new MobEffectInstance(mobEffectInstance));
					break;
				}
			}
			else {
				if (this.random.nextFloat() < ThirstyWitches.waterBreathingChance && this.isEyeInFluid(FluidTags.WATER) && !this.hasEffect(MobEffects.WATER_BREATHING) && this.getAirSupply() < this.getMaxAirSupply() / 2) {
					mobEffectInstances.addAll(Potions.WATER_BREATHING.getEffects());
				}
				else if (this.random.nextFloat() < ThirstyWitches.fireResistanceChance && (this.isOnFire() || this.getLastDamageSource() != null && this.getLastDamageSource().is(DamageTypeTags.IS_FIRE)) && !this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
					mobEffectInstances.addAll(Potions.FIRE_RESISTANCE.getEffects());
				}
				else if (this.random.nextFloat() < ThirstyWitches.healingChance && this.getHealth() < this.getMaxHealth() - 3) {
					if (this.getHealth() < this.getMaxHealth() * ThirstyWitches.strongHealingThreshold)
						mobEffectInstances.addAll(Potions.STRONG_HEALING.getEffects());
					else
						mobEffectInstances.addAll(Potions.HEALING.getEffects());
				}
			}

			ItemStack item = null;
			if (!mobEffectInstances.isEmpty()) {
				item = MCUtils.setCustomEffects(new ItemStack(Items.POTION), mobEffectInstances);
			}
			else if (MCUtils.hasLongNegativeEffect(this) && ThirstyWitches.shouldDrinkMilk(this.random)) {
				item = new ItemStack(Items.MILK_BUCKET);
			}

			if (item != null) {
				this.setItemSlot(EquipmentSlot.MAINHAND, item);
				this.usingTime = this.getMainHandItem().getUseDuration();
				this.setUsingItem(true);
				if (!this.isSilent()) {
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
				}

				AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
				attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
				attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
			}
		}

		if (WitchPotionThrowing.shouldUseSlowFalling() && this.fallDistance > 8 && !this.hasEffect(MobEffects.SLOW_FALLING)) {
			ItemStack stack = MCUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0)));
			this.getLookControl().setLookAt(this.getX(), this.getY(), this.getZ());
			if (!this.isSilent()) {
				this.playSound(SoundEvents.WITCH_THROW, 1.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
			}
			this.level().levelEvent(2002, this.blockPosition(), PotionUtils.getColor(stack));
			List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(stack);
			for (MobEffectInstance mobEffect : mobEffects) {
				this.addEffect(new MobEffectInstance(mobEffect));
			}
		}

		if (!this.hasEffect(MobEffects.INVISIBILITY) && --this.invisibilityCooldown <= 0 && this.getHealth() < this.getMaxHealth() * WitchPotionThrowing.healthThresholdInvisibility) {
			ThrownPotion thrownPotion = new ThrownPotion(this.level(), this);
			thrownPotion.setItem(MCUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(new MobEffectInstance(MobEffects.INVISIBILITY, 200))));
			thrownPotion.shoot(0, -1d, 0, 0.1f, 2f);
			this.level().addFreshEntity(thrownPotion);

			//Try 5 times to find a random spot
			for (int i = 0; i < 5; i++) {
				Vec3 randomPos = DefaultRandomPos.getPos(this, 16, 9);
				if (randomPos != null) {
					this.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, 1.1f);
					break;
				}
			}

			this.invisibilityCooldown = 20;
		}

		if (this.random.nextFloat() < 7.5E-4F) {
			this.level().broadcastEntityEvent(this, (byte)15);
		}

		super.aiStep();
	}

	@Shadow
	public abstract boolean isDrinkingPotion();

	@Shadow
	public abstract void setUsingItem(boolean using);
}
