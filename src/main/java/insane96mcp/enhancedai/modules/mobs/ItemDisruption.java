package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Item Disruption", description = "Endermen will make the player's item fall from his hands. Add/remove mobs via the enhancedai:can_disrupt_item entity type tag")
@LoadFeature(module = Modules.Ids.MOBS)
public class ItemDisruption extends Feature {
    public static final TagKey<EntityType<?>> CAN_DISRUPT_ITEM = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_disrupt_item"));
    public static final String ITEM_DISRUPTION_CHANCE = EnhancedAI.RESOURCE_PREFIX + "item_disruption_chance";

    @Config(min = 0d, max = 1d)
    @Label(name = "Chance", description = "Chance can be changed within entity data's ForgeData.\"enhancedai:item_disruption_chance\"")
    public static Difficulty chance = new Difficulty(0.25d, 0.25d, 0.35d);

    public ItemDisruption(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onHit(LivingDamageEvent event) {
        if (!this.isEnabled()
                //TODO allow for any mob
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getDirectEntity() instanceof Mob mob))
            return;

        if (mob.getRandom().nextFloat() < mob.getPersistentData().getFloat(ITEM_DISRUPTION_CHANCE)) {
            ItemStack stack;
            ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (mainHandItem.isEmpty() && offHandItem.isEmpty())
                return;

            if (!mainHandItem.isEmpty()) {
                if (!offHandItem.isEmpty())
                    stack = mob.getRandom().nextBoolean() ? mainHandItem.copy() : offHandItem.copy();
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
            double x = player.getX() - mob.getX();
            double z = player.getZ() - mob.getZ();
            Vec2 dir = new Vec2((float) x, (float) z).normalized();
            if (mob.getRandom().nextBoolean())
                itementity.setDeltaMovement(itementity.getDeltaMovement().add(-dir.y * 0.4f, 0, dir.x * 0.4f));
            else
                itementity.setDeltaMovement(itementity.getDeltaMovement().add(dir.y * 0.4f, 0, -dir.x * 0.4f));
            itementity.setPickUpDelay(40);
            player.getCommandSenderWorld().addFreshEntity(itementity);
        }
    }

    @SubscribeEvent
    public void onJoinLevelEvent(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_DISRUPT_ITEM)
                || mob.getPersistentData().contains(ITEM_DISRUPTION_CHANCE))
            return;

        mob.getPersistentData().putFloat(ITEM_DISRUPTION_CHANCE, (float) chance.getByDifficulty(mob.level()));
    }
}