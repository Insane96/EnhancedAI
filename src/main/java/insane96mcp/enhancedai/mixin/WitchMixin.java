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
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Witch.class)
public abstract class WitchMixin extends Raider {
	@Shadow
	private int usingTime;

	protected WitchMixin(EntityType<? extends Raider> p_37839_, Level p_37840_) {
		super(p_37839_, p_37840_);
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
		if (original && !this.isSilent() && ThirstyWitches.playSoundWhenDrinking && this.usingTime % 4 == 0)
			this.playSound(SoundEvents.GENERIC_DRINK, 0.75f, this.random.nextFloat() * 0.1F + 0.9F);
		return original;
	}

	@Unique
	private ItemStack enhancedai$stackToUse;

	@Definition(id = "holder", local = @Local(type = Holder.class))
	@Expression("holder != null")
	@ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean enhancedai$newUseItem(boolean original) {
		if (!Feature.isEnabled(ThirstyWitches.class)
				|| !this.getType().is(ThirstyWitches.AFFECTED_ENTITY_TYPES))
			return original;

		enhancedai$stackToUse = ItemStack.EMPTY;
		double distance = ThirstyWitches.CUSTOM_DRINK_DISTANCE_SAFE.get(this);
		distance *= distance;
		if (this.getTarget() != null && this.getTarget() instanceof Player && this.distanceToSqr(this.getTarget()) > distance) {
			for (PotionOrMobEffect potionOrMobEffect : ThirstyWitches.drinkPotion.entries) {
				if (potionOrMobEffect.hasMobEffect(this))
					continue;

				enhancedai$stackToUse = potionOrMobEffect.getPotionStack();
				break;
			}
		}
		else {
			Holder<Potion> potion = null;
			if (this.random.nextFloat() < ThirstyWitches.WATER_BREATHING_CHANCE.get(this)
					&& this.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())
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
				enhancedai$stackToUse = PotionContents.createItemStack(Items.POTION, potion);
		}

		if (enhancedai$stackToUse.isEmpty() && MCUtils.hasLongNegativeEffect(this) && this.random.nextDouble() < ThirstyWitches.MILK_CHANCE.get(this))
			enhancedai$stackToUse = new ItemStack(Items.MILK_BUCKET);
		return !enhancedai$stackToUse.isEmpty();
	}

	@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack enhancedai$changeUseItem(Item item, Holder<Potion> holder, Operation<ItemStack> original) {
		if (!Feature.isEnabled(ThirstyWitches.class)
				|| !this.getType().is(ThirstyWitches.AFFECTED_ENTITY_TYPES)
				|| enhancedai$stackToUse.isEmpty())
			return original.call(item, holder);

		return enhancedai$stackToUse;
	}
}
