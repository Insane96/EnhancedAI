package insane96mcp.enhancedai.data;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PotionOrMobEffect {
	Holder<Potion> potion;
	MobEffectInstance mobEffectInstance;

	public PotionOrMobEffect(Holder<Potion> potion) {
		this.potion = potion;
	}

	public PotionOrMobEffect(MobEffectInstance mobEffectInstance) {
		this.mobEffectInstance = mobEffectInstance;
	}

	public ItemStack getPotionStack() {
		return getStackInternal(Items.POTION);
	}

	public ItemStack getSplashPotionStack() {
		return getStackInternal(Items.SPLASH_POTION);
	}

	public ItemStack getLingeringPotionStack() {
		return getStackInternal(Items.LINGERING_POTION);
	}

	private ItemStack getStackInternal(Item item) {
		ItemStack stack;
		if (this.potion != null)
			stack = PotionContents.createItemStack(item, this.potion);
		else
			stack = MCUtils.createPotionStackFromEffectInstances(item, List.of(new MobEffectInstance(this.mobEffectInstance)));

		return stack;
	}

	public List<Holder<MobEffect>> getMobEffects() {
		List<Holder<MobEffect>> mobEffects = new ArrayList<>();
		if (this.potion != null) {
			for (MobEffectInstance mobEffectInstance1 : this.potion.value().getEffects()) {
				mobEffects.add(mobEffectInstance1.getEffect());
			}
		}
		else {
			mobEffects.add(this.mobEffectInstance.getEffect());
		}
		return mobEffects;
	}

	public boolean hasMobEffect(LivingEntity living) {
		List<Holder<MobEffect>> mobEffects = this.getMobEffects();
		for (MobEffectInstance mobEffect : living.getActiveEffects()) {
			if (mobEffects.contains(mobEffect.getEffect()))
				return true;
		}
		return false;
	}

	public static ArrayList<PotionOrMobEffect> parseList(List<? extends String> list) {
		ArrayList<PotionOrMobEffect> potionOrMobEffects = new ArrayList<>();
		for (String s : list) {
			Holder<Potion> potion = parsePotion(s);
			if (potion != null) {
				potionOrMobEffects.add(new PotionOrMobEffect(potion));
			}
			else {
				MobEffectInstance mobEffectInstance = MCUtils.parseEffectInstance(s);
				if (mobEffectInstance != null)
					potionOrMobEffects.add(new PotionOrMobEffect(mobEffectInstance));
				else
					EnhancedAI.LOGGER.warn("{} is not a valid potion or a mob effect instance", s);
			}
		}
		return potionOrMobEffects;
	}

	/**
	 * Parses a string to Potion
	 */
	@Nullable
	public static Holder<Potion> parsePotion(String s) {
		ResourceLocation effectRL = ResourceLocation.tryParse(s);
		if (effectRL == null)
			return null;
		var holder = BuiltInRegistries.POTION.getHolder(effectRL);
		if (holder.isEmpty()) {
			EnhancedAI.LOGGER.warn("Potion {} not found", effectRL);
			return null;
		}
		return holder.get();
	}

    public Holder<Potion> getPotion() {
        return this.potion;
    }

    public String serialize() {
        if (this.potion != null) {
            return BuiltInRegistries.POTION.getKey(this.potion.value()).toString();
        }
        else {
            String effectId = BuiltInRegistries.MOB_EFFECT.getKey(this.mobEffectInstance.getEffect().value()).toString();
            return effectId + "," + this.mobEffectInstance.getDuration() + "," + this.mobEffectInstance.getAmplifier();
        }
    }
}
