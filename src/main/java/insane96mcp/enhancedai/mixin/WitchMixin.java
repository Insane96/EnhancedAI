package insane96mcp.enhancedai.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.enhancedai.data.PotionOrMobEffect;
import insane96mcp.enhancedai.modules.witch.ThirstyWitches;
import insane96mcp.enhancedai.modules.witch.darkart.DarkArt;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestHealableRaiderTargetGoal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	@Unique
    private int enhancedai$invisibilityCooldown = 20;

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

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isAlive()Z"))
	public boolean onIsAlive(boolean alive) {
		return alive && !ModNBTData.get(this, DarkArt.PERFORMING_DARK_ARTS, Boolean.class);
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean enhancedai$drinkMilk(ItemStack instance, Item pItem, Operation<Boolean> original) {
		boolean ret = original.call(instance, pItem);
		if (!ret && instance.is(Items.MILK_BUCKET))
			Items.MILK_BUCKET.finishUsingItem(instance, this.level(), this);
		return ret;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isDrinkingPotion()Z"))
	public boolean enhancedai$playSoundWhenDrinking(boolean original) {
		if (original && ThirstyWitches.playSoundWhenDrinking && this.usingTime % 4 == 0)
			this.playSound(SoundEvents.GENERIC_DRINK, 0.75f, this.random.nextFloat() * 0.1F + 0.9F);
		return original;
	}

	@Unique
	private ItemStack enhancedai$stackToUse;

	@Definition(id = "potion", local = @Local(type = Potion.class))
	@Expression("potion != null")
	@ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean enhancedai$newUseItem(boolean original) {
		if (!Feature.isEnabled(ThirstyWitches.class)
				|| !this.getType().is(ThirstyWitches.AFFECTED_ENTITY_TYPES))
			return original;

		enhancedai$stackToUse = ItemStack.EMPTY;
		double distance = ThirstyWitches.CUSTOM_DRINK_DISTANCE_SAFE.get(this);
		distance *= distance;
		if (this.getTarget() != null && this.getTarget() instanceof Player && this.distanceToSqr(this.getTarget()) > distance) {
			for (PotionOrMobEffect potionOrMobEffect : ThirstyWitches.drinkPotion) {
				MobEffect mobEffect = potionOrMobEffect.getMobEffect();
				if (mobEffect != null && this.hasEffect(mobEffect))
					continue;

				enhancedai$stackToUse = potionOrMobEffect.getPotionStack();
				break;
			}
		}
		else {
			Potion potion = null;
			if (this.random.nextFloat() < ThirstyWitches.WATER_BREATHING_CHANCE.get(this)
					&& this.isEyeInFluidType(ForgeMod.WATER_TYPE.get())
					&& !this.hasEffect(MobEffects.WATER_BREATHING)
					&& this.getAirSupply() < this.getMaxAirSupply() / 2)
				potion = Potions.WATER_BREATHING;
			else if (this.random.nextFloat() < ThirstyWitches.FIRE_RESISTANCE_CHANCE.get(this)
					&& (this.isOnFire() || this.getLastDamageSource() != null && this.getLastDamageSource().is(DamageTypeTags.IS_FIRE))
					&& !this.hasEffect(MobEffects.FIRE_RESISTANCE))
				potion = Potions.FIRE_RESISTANCE;
			else if (this.getHealth() / this.getMaxHealth() < ThirstyWitches.HEALING_THRESHOLD.get(this)
					&& this.random.nextFloat() < ThirstyWitches.HEALING_CHANCE.get(this)) {
				potion = Potions.HEALING;
				if (this.getHealth() / this.getMaxHealth() < ThirstyWitches.STRONG_HEALING_THRESHOLD.get(this))
					potion = Potions.STRONG_HEALING;
			}
			if (potion != null)
				enhancedai$stackToUse = PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
		}

		if (enhancedai$stackToUse.isEmpty() && MCUtils.hasLongNegativeEffect(this) && this.random.nextDouble() < ThirstyWitches.MILK_CHANCE.get(this))
			enhancedai$stackToUse = new ItemStack(Items.MILK_BUCKET);
		return !enhancedai$stackToUse.isEmpty();
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 1))
	private void enhancedai$changeUseItem(Witch instance, EquipmentSlot equipmentSlot, ItemStack itemStack, Operation<Void> original) {
		if (!Feature.isEnabled(ThirstyWitches.class)
				|| !this.getType().is(ThirstyWitches.AFFECTED_ENTITY_TYPES)) {
			original.call(instance, equipmentSlot, itemStack);
			return;
		}

		original.call(instance, equipmentSlot, enhancedai$stackToUse);
	}

	@Inject(at = @At("HEAD"), method = "aiStep", cancellable = true)
	private void aiStep(CallbackInfo ci) {
/*
		if (this.isDrinkingPotion()) {

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

			if nothing to drink {
				if (WitchPotionThrowing.shouldUseSlowFalling() && this.fallDistance > 8 && !this.hasEffect(MobEffects.SLOW_FALLING)) {
					ItemStack slowFallingStack = MCUtils.setCustomEffects(new ItemStack(Items.SPLASH_POTION), List.of(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0)));
					this.getLookControl().setLookAt(this.getX(), this.getY(), this.getZ());
					if (!this.isSilent()) {
						this.playSound(SoundEvents.WITCH_THROW, 1.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
					}
					this.level().levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, this.blockPosition(), PotionUtils.getColor(slowFallingStack));
					List<MobEffectInstance> mobEffects = PotionUtils.getMobEffects(slowFallingStack);
					for (MobEffectInstance mobEffect : mobEffects) {
						this.addEffect(new MobEffectInstance(mobEffect));
					}
				}

				if (!this.hasEffect(MobEffects.INVISIBILITY) && this.onGround() && --this.enhancedAI$invisibilityCooldown <= 0 && this.getHealth() < this.getMaxHealth() * WitchPotionThrowing.healthThresholdInvisibility) {
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

					this.enhancedAI$invisibilityCooldown = 20;
				}
			}
		}

		if (this.random.nextFloat() < 7.5E-4F) {
			this.level().broadcastEntityEvent(this, EntityEvent.WITCH_HAT_MAGIC);
		}

		super.aiStep();*/
	}

	@Shadow
	public abstract boolean isDrinkingPotion();

	@Shadow
	public abstract void setUsingItem(boolean using);
}
