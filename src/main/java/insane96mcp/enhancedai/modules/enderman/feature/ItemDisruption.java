package insane96mcp.enhancedai.modules.enderman.feature;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Item Disruption", description = "Endermen will make the player's item fall from his hands.")
@LoadFeature(module = Modules.Ids.ENDERMAN, enabledByDefault = false)
public class ItemDisruption extends Feature {

    @Config(min = 0d, max = 1d)
    @Label(name = "Chance")
    public static Difficulty chance = new Difficulty(0.25d, 0.25d, 0.35d);

    public ItemDisruption(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onHit(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getDirectEntity() instanceof EnderMan enderman))
            return;

        if (enderman.getRandom().nextFloat() < chance.getByDifficulty(player.level())) {
            ItemStack stack;
            ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (mainHandItem.isEmpty() && offHandItem.isEmpty())
                return;

            if (!mainHandItem.isEmpty()) {
                if (!offHandItem.isEmpty())
                    stack = enderman.getRandom().nextBoolean() ? mainHandItem.copy() : offHandItem.copy();
                else
                    stack = mainHandItem.copy();
            }
            else
                stack = offHandItem.copy();

            event.setCanceled(true);
            player.level().playSound(null, player, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
            Inventory inventory = player.getInventory();
            int slot = inventory.findSlotMatchingItem(stack);
            if (slot == -1)
                slot = Inventory.SLOT_OFFHAND;
            inventory.removeItem(slot, stack.getCount());
            if (player.getUseItem() == stack && stack.getCount() == 1)
                player.stopUsingItem(); // Forge: fix MC-231097 on the serverside
            player.containerMenu.findSlot(inventory, slot).ifPresent((i) -> {
                player.containerMenu.setRemoteSlot(i, inventory.getItem(i));
                player.containerMenu.sendAllDataToRemote();
            });
            ItemEntity itementity = new ItemEntity(player.level(), player.getX(), player.getY() + player.getBbHeight() / 2f, player.getZ(), stack);
            itementity.setPickUpDelay(40);
            player.getCommandSenderWorld().addFreshEntity(itementity);
        }
    }
}