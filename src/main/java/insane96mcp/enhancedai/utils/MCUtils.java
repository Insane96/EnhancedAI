package insane96mcp.enhancedai.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

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

    /**
     * Same as hasNegativeEffect but also checks if the duration of the effect is higher than 7.5 seconds
     * @param entity
     * @return
     */
    public static boolean hasLongNegativeEffect(LivingEntity entity) {
        for (MobEffectInstance mobEffectInstance : entity.getActiveEffects()) {
            if (entity.hasEffect(mobEffectInstance.getEffect()) && mobEffectInstance.getEffect().getCategory().equals(MobEffectCategory.HARMFUL) && mobEffectInstance.getDuration() > 150)
                return true;
        }
        return false;
    }

    /**
     * Returns level.getMinBuildHeight() - 1 when no spawn spots are found, otherwise the Y coord
     */
    public static int getYSpawn(EntityType<?> entityType, BlockPos pos, Level level, int minRelativeY) {
        int height = (int) Math.ceil(entityType.getHeight());
        int fittingYPos = level.getMinBuildHeight() - 1;
        for (int y = pos.getY(); y > pos.getY() - minRelativeY; y--) {
            boolean viable = true;
            BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
            for (int i = 0; i < height; i++) {
                if (level.getBlockState(p.above(i)).getMaterial().blocksMotion()) {
                    viable = false;
                    break;
                }
            }
            if (!viable)
                continue;
            fittingYPos = y;
            if (!level.getBlockState(p.below()).getMaterial().blocksMotion())
                continue;
            return y;
        }
        return fittingYPos;
    }
}
