package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagValue;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = Modules.Ids.MOBS, description = "Gives mobs a chance to negate damage when equipped with a shield. Only entity types in `enhancedai:mobs/can_equip_shield` tag will be equipped a shield.")
public class Shielding extends JsonFeature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_equip_shield"));

	public static final List<IdTagValue> DEFAULT_SHIELD_BLOCK_CHANCE = List.of(
			IdTagValue.newId("minecraft:shield", 0.2d),
			IdTagValue.newId("shieldsplus:wooden_shield", 0.1d),
			IdTagValue.newId("shieldsplus:stone_shield", 0.15d),
			IdTagValue.newId("shieldsplus:iron_shield", 0.2d),
			IdTagValue.newId("shieldsplus:golden_shield", 0.1d),
			IdTagValue.newId("shieldsplus:diamond_shield", 0.25d),
			IdTagValue.newId("shieldsplus:netherite_shield", 0.35d),
			IdTagValue.newId("iguanatweaksreborn:copper_shield", 0.15d)
	);
	public static final List<IdTagValue> shieldBlockChance = new ArrayList<>();

	@Config
	public static double chanceToEquip = 0.08d;

	public static ResourceLocation HAS_SHIELD_BEEN_GIVEN;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		HAS_SHIELD_BEEN_GIVEN = this.createDataKey("has_shield_been_given");
		JSON_CONFIGS.add(new JsonConfig<>("shield_block_chance.json", shieldBlockChance, DEFAULT_SHIELD_BLOCK_CHANCE, IdTagValue.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Mob mob)
				|| ModNBTData.get(mob, HAS_SHIELD_BEEN_GIVEN, Boolean.class)
				|| mob.level().isClientSide
				|| !mob.getType().is(AFFECTED_ENTITY_TYPES))
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
                || !(event.getEntity() instanceof Mob)
				|| attacked.level().isClientSide)
            return;

		float chance = 0f;
		ItemStack offHandItem = attacked.getOffhandItem();
		for (IdTagValue shieldChance : shieldBlockChance) {
			if (shieldChance.id.matchesItem(offHandItem)) {
				chance = (float) shieldChance.value;
				break;
			}
		}
		if (chance == 0f)
			return;
		//TODO Add disabling shield
		/*if (event.getSource().getDirectEntity() instanceof LivingEntity attacker
				&& attacker.getMainHandItem().is(ItemTags.AXES)
				&& attacked.getRandom().nextFloat() < 0.75f) {

			this.getCooldowns().addCooldown(this.getUseItem().getItem(), 100);
			this.stopUsingItem();
			this.level().broadcastEntityEvent(this, (byte)30);
		}
		else*/ if (attacked.getRandom().nextDouble() < chance) {
			event.setCanceled(true);
			offHandItem.hurt((int) event.getAmount(), attacked.getRandom(), null);
			attacked.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + attacked.level().random.nextFloat() * 0.4F);
		}
    }
}