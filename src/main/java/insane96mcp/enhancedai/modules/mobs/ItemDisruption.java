package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = EAIModules.Ids.MOBS, enabledByDefault = false, description = "Endermen will make the player's item fall from his hands. Add/remove mobs via the enhancedai:mobs/can_disrupt_item entity type tag")
public class ItemDisruption extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_disrupt_item"));

    @Config(min = 0d, max = 1d)
    public static DifficultyBasedConfig chance = new DifficultyBasedConfig(0.25d, 0.25d, 0.35d);

    @Config(description = "Cooldown (in ticks) before being able to use the ability again.")
    public static Integer cooldown = 200;

	public static ResourceLocation LAST_DISRUPTION;
	public static EAIData<Double> CHANCE;
	public static EAIData<Integer> COOLDOWN;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		LAST_DISRUPTION = this.createDataKey("last_disruption");
		CHANCE = EAIData.ofDouble(this.createDataKey("chance"));
		COOLDOWN = EAIData.ofInt(this.createDataKey("cooldown"));
    }

    @SubscribeEvent
    public void onHit(LivingDamageEvent.Pre event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getDirectEntity() instanceof Mob mob))
            return;

        if (mob.level().getGameTime() - ModNBTData.get(mob, LAST_DISRUPTION, Long.class) < COOLDOWN.get(mob))
            return;

        if (mob.getRandom().nextFloat() >= CHANCE.get(mob))
            return;

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

        event.setNewDamage(0);
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
        /*double x = player.getX() - mob.getX();
        double z = player.getZ() - mob.getZ();
        Vec2 dir = new Vec2((float) x, (float) z).normalized();
        if (mob.getRandom().nextBoolean())
            itementity.setDeltaMovement(itementity.getDeltaMovement().add(-dir.y * 0.4f, 0.1f, dir.x * 0.4f));
        else
            itementity.setDeltaMovement(itementity.getDeltaMovement().add(dir.y * 0.4f, 0.1f, -dir.x * 0.4f));*/
        itementity.setPickUpDelay(30);
        mob.level().addFreshEntity(itementity);
        ModNBTData.put(mob, LAST_DISRUPTION, mob.level().getGameTime());
    }

    @SubscribeEvent
    public void onJoinLevelEvent(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

		CHANCE.applyIfAbsent(mob, chance.getByDifficulty(mob.level()));
		COOLDOWN.applyIfAbsent(mob, cooldown);
    }
}