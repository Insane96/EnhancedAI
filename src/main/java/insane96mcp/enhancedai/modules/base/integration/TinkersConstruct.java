package insane96mcp.enhancedai.modules.base.integration;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.TinkerHooks;
import slimeknights.tconstruct.library.modifiers.hook.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.NamespacedNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TinkersConstruct {
    public static void setProjectileModifiers(ItemStack stack, LivingEntity shooter, AbstractArrow arrow) {
        ToolStack tool = ToolStack.from(stack);
        // vanilla arrows have a base damage of 2, cancel that out then add in our base damage to account for custom arrows with higher base damage
        // calculate it just once as all four arrows are the s ame item, they should have the same damage
        float baseArrowDamage = (float)(arrow.getBaseDamage() - 2 + tool.getStats().get(ToolStats.PROJECTILE_DAMAGE));
        arrow.setBaseDamage(ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.PROJECTILE_DAMAGE, baseArrowDamage));

        // just store all modifiers on the tool for simplicity
        ModifierNBT modifiers = tool.getModifiers();
        arrow.getCapability(EntityModifierCapability.CAPABILITY).ifPresent(cap -> cap.setModifiers(modifiers));

        // fetch the persistent data for the arrow as modifiers may want to store data
        NamespacedNBT arrowData = PersistentDataCapability.getOrWarn(arrow);

        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

        // let modifiers such as fiery and punch set properties
        for (ModifierEntry entry : modifiers.getModifiers()) {
            entry.getHook(TinkerHooks.PROJECTILE_LAUNCH).onProjectileLaunch(tool, entry, shooter, arrow, arrow, arrowData, true);
        }
    }
}
