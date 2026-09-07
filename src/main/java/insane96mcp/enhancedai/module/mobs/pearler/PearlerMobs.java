package insane96mcp.enhancedai.module.mobs.pearler;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.MOBS, description = "Let mobs use ender pearls as long as they have them in the either hands and when far enough from the target. Only mobs in the enhancedai:mobs/can_equip_pearl entity type tag will try to be equipped ender pearls.")
public class PearlerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_EQUIP_PEARL = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_equip_pearl"));

	@Config(min = 0d, max = 1d, description = "Chance for a mob to spawn with Ender Pearls in the offhand.\nI recommend Mobs Properties Randomness to have more control over mobs equipment.")
	public static Double equipEnderPearlChance = 0.05;
	@Config(min = 0, max = 16, description = "How many ender pearls will Mobs spawn with.")
	public static MinMaxConfig enderPearlAmount = new MinMaxConfig(2, 4);
	@Config(min = 1, max = 16, description = "Inaccuracy when throwing the ender pearl.")
	public static Integer inaccuracy = 3;

	public static ResourceLocation HAS_ENDER_PEARL_BEEN_GIVEN;
	public static EAIData<Integer> INACCURACY;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		HAS_ENDER_PEARL_BEEN_GIVEN = this.createDataKey("has_ender_pearl_been_given");
		INACCURACY = EAIData.ofInt(this.createDataKey("inaccuracy"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| ModNBTData.get(mob, HAS_ENDER_PEARL_BEEN_GIVEN, Boolean.class))
			return;

		if (mob.getOffhandItem().isEmpty() && mob.getRandom().nextDouble() < equipEnderPearlChance && mob.getType().is(CAN_EQUIP_PEARL))
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL, enderPearlAmount.getIntRandBetween(mob.getRandom())));

		ModNBTData.put(mob, HAS_ENDER_PEARL_BEEN_GIVEN, true);
		mob.goalSelector.addGoal(2, new PearlUseGoal(mob));
		INACCURACY.applyIfAbsent(mob, inaccuracy);
	}
}
