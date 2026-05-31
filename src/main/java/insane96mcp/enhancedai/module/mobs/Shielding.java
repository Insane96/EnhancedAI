package insane96mcp.enhancedai.module.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.ObjTagValue;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = EAIModules.MOBS, description = "Gives mobs a chance to negate damage when equipped with a shield. Only entity types in `enhancedai:mobs/can_equip_shield` tag will be equipped a shield.")
public class Shielding extends JsonFeature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_equip_shield"));

	public static final List<ObjTagValue<Item>> DEFAULT_SHIELD_BLOCK_CHANCE = List.of(
			ObjTagValue.of("minecraft:shield", 0.20d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:wooden_shield", 0.10d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:stone_shield", 0.15d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:copper_shield", 0.15d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:iron_shield", 0.20d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:golden_shield", 0.10d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:diamond_shield", 0.30d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:netherite_shield", 0.35d, Registries.ITEM),
			ObjTagValue.of("shieldsplus:copper_shield", 0.15d, Registries.ITEM)
	);
	public static final List<ObjTagValue<Item>> shieldBlockChance = new ArrayList<>();

	@Config(min = 0, max = 1)
	public static double chanceToEquip = 0.08d;

	public static ResourceLocation HAS_SHIELD_BEEN_GIVEN;
    public static ResourceLocation LAST_HURT_BY_AXE;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		HAS_SHIELD_BEEN_GIVEN = this.createDataKey("has_shield_been_given");
        LAST_HURT_BY_AXE = this.createDataKey("last_hurt_by_axe");
		this.getJsonConfigs().add(new JsonConfig<>("shield_block_chance.json", shieldBlockChance, DEFAULT_SHIELD_BLOCK_CHANCE, ObjTagValue.LIST_TYPE).withRegistryFor(Registries.ITEM));
	}

	@Override
	public String getModConfigFolder() {
		return EnhancedAI.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
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
    public void onMobAttacked(LivingIncomingDamageEvent event) {
		LivingEntity attacked = event.getEntity();
		if (!this.isEnabled()
				|| event.getSource().is(DamageTypeTags.BYPASSES_SHIELD)
                || !(event.getEntity() instanceof Mob)
                || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
				|| attacked.level().isClientSide)
            return;

        long lastHurtByAxe = ModNBTData.get(attacked, LAST_HURT_BY_AXE, Long.class);
        if (lastHurtByAxe > 0L && attacked.level().getGameTime() - lastHurtByAxe < 32L)
            return;

		float chance = 0f;
		ItemStack offHandItem = attacked.getOffhandItem();
		for (ObjTagValue<Item> shieldChance : shieldBlockChance) {
			if (shieldChance.id.matches(offHandItem.getItem())) {
				chance = (float) shieldChance.value;
				break;
			}
		}
		if (chance == 0f)
			return;
		if (attacked.getRandom().nextDouble() < chance) {
			event.setCanceled(true);
			offHandItem.hurtAndBreak((int) event.getAmount(), attacker, EquipmentSlot.OFFHAND);
            if (attacker.getMainHandItem().is(ItemTags.AXES)) {
                ModNBTData.put(attacked, LAST_HURT_BY_AXE, attacked.level().getGameTime());
                attacked.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + attacked.level().random.nextFloat() * 0.4F);
            }
            else {
                attacked.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + attacked.level().random.nextFloat() * 0.4F);
            }
		}
    }
}