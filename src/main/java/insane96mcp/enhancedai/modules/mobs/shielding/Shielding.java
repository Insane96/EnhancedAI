package insane96mcp.enhancedai.modules.mobs.shielding;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//TODO JsonFeature to configure chance to block per shield
@LoadFeature(module = Modules.Ids.MOBS, description = "Gives mobs a chance to negate damage when equipped with a shield.")
public class Shielding extends Feature {

	@Config(description = "Chance for entity types in the `enhancedai:shielding/can_equip_shield` tag to spawn with a shield in the offhand.")
	public static double chanceToEquip = 0.08d;
	@Config
	public static double chanceToBlock = 0.20d;

	public static ResourceLocation HAS_SHIELD_BEEN_GIVEN;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		HAS_SHIELD_BEEN_GIVEN = this.createDataKey("has_shield_been_given");
    }

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Mob mob)
				|| ModNBTData.get(mob, HAS_SHIELD_BEEN_GIVEN, Boolean.class)
				|| mob.level().isClientSide)
			return;

		if (mob.getRandom().nextDouble() < chanceToEquip)
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		ModNBTData.put(mob, HAS_SHIELD_BEEN_GIVEN, true);
	}

    @SubscribeEvent
    public void onMobAttacked(LivingAttackEvent event) {
		LivingEntity attacked = event.getEntity();
		if (!this.isEnabled()
				|| event.getSource().is(DamageTypeTags.BYPASSES_SHIELD)
				|| !(attacked.getOffhandItem().getItem() instanceof ShieldItem)
				|| attacked.level().isClientSide)
            return;

		//TODO Add disabling shield
		/*if (event.getSource().getDirectEntity() instanceof LivingEntity attacker
				&& attacker.getMainHandItem().is(ItemTags.AXES)
				&& attacked.getRandom().nextFloat() < 0.75f) {

			this.getCooldowns().addCooldown(this.getUseItem().getItem(), 100);
			this.stopUsingItem();
			this.level().broadcastEntityEvent(this, (byte)30);
		}
		else*/ if (attacked.getRandom().nextDouble() < chanceToBlock) {
			event.setCanceled(true);
			attacked.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + attacked.level().random.nextFloat() * 0.4F);
		}
    }
}