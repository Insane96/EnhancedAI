package insane96mcp.enhancedai.utils;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class MCUtils {
    /**
     * Returns the slot (main hand or offhand) in which the entity is holding the given item.
     *
     * @param entity the entity whose hands should be checked
     * @param item the item to look for
     * @return {@link EquipmentSlot#MAINHAND} or {@link EquipmentSlot#OFFHAND} if the item is held, {@code null} otherwise
     */
    @Nullable
    public static EquipmentSlot getEquipmentSlot(LivingEntity entity, Item item) {
        if (entity.getMainHandItem().is(item))
            return EquipmentSlot.MAINHAND;
        else if (entity.getOffhandItem().is(item))
            return EquipmentSlot.OFFHAND;
        return null;
    }

    /**
     * Returns the slot (main hand or offhand) in which the entity is holding an item with the given tag.
     *
     * @param entity the entity whose hands should be checked
     * @param itemTag the item tag to look for
     * @return {@link EquipmentSlot#MAINHAND} or {@link EquipmentSlot#OFFHAND} if an item with that tag is held, {@code null} otherwise
     */
    @Nullable
    public static EquipmentSlot getEquipmentSlot(LivingEntity entity, TagKey<Item> itemTag) {
        if (entity.getMainHandItem().is(itemTag))
            return EquipmentSlot.MAINHAND;
        else if (entity.getOffhandItem().is(itemTag))
            return EquipmentSlot.OFFHAND;
        return null;
    }
}
