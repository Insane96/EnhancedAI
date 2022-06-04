package insane96mcp.enhancedai.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.Collection;

public class MCUtils {
    /**
     * Copy-paste of PotionUtils.setCustomEffects but setting the potion color too
     */
    public static ItemStack setCustomEffects(ItemStack itemStack, Collection<MobEffectInstance> mobEffectInstances) {
        if (!mobEffectInstances.isEmpty()) {
            CompoundTag compoundtag = itemStack.getOrCreateTag();
            ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

            for (MobEffectInstance mobeffectinstance : mobEffectInstances) {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
            }
            compoundtag.putInt(PotionUtils.TAG_CUSTOM_POTION_COLOR, PotionUtils.getColor(mobEffectInstances));

            compoundtag.put("CustomPotionEffects", listtag);

            itemStack.setHoverName(new TranslatableComponent("unknown_potion"));
        }
        return itemStack;
    }

    public static boolean hasNegativeEffect(LivingEntity entity) {
        for (MobEffectInstance mobEffectInstance : entity.getActiveEffects()) {
            if (entity.hasEffect(mobEffectInstance.getEffect()) && mobEffectInstance.getEffect().getCategory().equals(MobEffectCategory.HARMFUL))
                return true;
        }
        return false;
    }
}
