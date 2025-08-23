package insane96mcp.enhancedai.modules.mobs.pearler;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Let mobs use ender pearls as long as they have them in the off hand and when far enough from the target. Only mobs in the enhancedai:mobs/can_equip_pearl entity type tag will try to be equipped ender pearls.")
public class PearlerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_EQUIP_PEARL = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_equip_pearl"));

	@Config(min = 0d, max = 1d, description = "Chance for a mob to spawn with Ender Pearls in the offhand.\nI recommend Mobs Properties Randomness to have more control over mobs equipment.")
	public static Double equipEnderPearlChance = 0.05;
	@Config(min = 0, max = 16, description = "How many ender pearls will Mobs spawn with.")
	public static MinMax enderPearlAmount = new MinMax(2, 4);
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
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_EQUIP_PEARL)
				|| ModNBTData.get(mob, HAS_ENDER_PEARL_BEEN_GIVEN, Boolean.class))
			return;

		if (mob.getOffhandItem().isEmpty() && mob.getRandom().nextDouble() < equipEnderPearlChance)
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL, enderPearlAmount.getIntRandBetween(mob.getRandom())));

		ModNBTData.put(mob, HAS_ENDER_PEARL_BEEN_GIVEN, true);
		mob.goalSelector.addGoal(2, new PearlUseGoal(mob));
		INACCURACY.applyIfAbsent(mob, inaccuracy);
	}
}
