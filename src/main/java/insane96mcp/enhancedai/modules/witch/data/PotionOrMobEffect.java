package insane96mcp.enhancedai.modules.witch.data;

import insane96mcp.enhancedai.utils.LogHelper;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PotionOrMobEffect {
	Potion potion;
	MobEffectInstance mobEffectInstance;

	public PotionOrMobEffect(Potion potion) {
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

	public ItemStack getStackInternal(Item item) {
		ItemStack stack;
		if (this.potion != null)
			stack = PotionUtils.setPotion(new ItemStack(item), this.potion);
		else
			stack = MCUtils.setCustomEffects(new ItemStack(item), List.of(new MobEffectInstance(this.mobEffectInstance)));

		return stack;
	}

	@Nullable
	public MobEffect getMobEffect() {
		List<MobEffect> mobEffects = new ArrayList<>();
		if (this.potion != null) {
			for (MobEffectInstance mobEffectInstance1 : this.potion.getEffects()) {
				mobEffects.add(mobEffectInstance1.getEffect());
			}
		}
		else {
			mobEffects.add(this.mobEffectInstance.getEffect());
		}
		return !mobEffects.isEmpty() ? mobEffects.get(0) : null;
	}

	public static ArrayList<PotionOrMobEffect> parseList(List<? extends String> list) {
		ArrayList<PotionOrMobEffect> potionOrMobEffects = new ArrayList<>();
		for (String s : list) {
			Potion potion = parsePotion(s);
			if (potion != null) {
				potionOrMobEffects.add(new PotionOrMobEffect(potion));
			}
			else {
				MobEffectInstance mobEffectInstance = MCUtils.parseEffectInstance(s);
				if (mobEffectInstance != null)
					potionOrMobEffects.add(new PotionOrMobEffect(mobEffectInstance));
				else
					LogHelper.warn("%s is not a valid potion or a mob effect instance", s);
			}
		}
		return potionOrMobEffects;
	}

	/**
	 * Parses a string to Potion
	 */
	@Nullable
	public static Potion parsePotion(String s) {
		ResourceLocation effectRL = ResourceLocation.tryParse(s);
		if (effectRL == null) {
			return null;
		}
		return ForgeRegistries.POTIONS.getValue(effectRL);
	}
}
