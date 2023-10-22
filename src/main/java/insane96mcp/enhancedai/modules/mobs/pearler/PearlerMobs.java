package insane96mcp.enhancedai.modules.mobs.pearler;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
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

@Label(name = "Pearler Mobs", description = "Let mobs use ender pearls. Either put ender pearls in main or off hand and when far enough from the target they will throw it. Only mobs in the enhancedai:can_be_pearler entity type tag can be pearler.")
@LoadFeature(module = Modules.Ids.MOBS)
public class PearlerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BE_PEARLER = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "can_be_pearler"));
	public static final String HAS_ENDER_PEARL_BEEN_GIVEN = EnhancedAI.RESOURCE_PREFIX + "has_ender_pearl_been_given";

	@Config(min = 0d, max = 1d)
	@Label(name = "Equip Ender Pearl Chance", description = "Chance for a mob in the entity type tag enhancedai:can_be_pearler to spawn with a Fishing Rod in the offhand.\nI recommend Mobs Properties Randomness to have more control over mobs equipment.")
	public static Double equipEnderPearlChance = 0.05;
	@Config(min = 0, max = 16)
	@Label(name = "Ender Pearl Amount", description = "How many ender pearls will Mobs spawn with.")
	public static Integer enderPearlAmount = 3;
	@Config(min = 1, max = 16)
	@Label(name = "Inaccuracy", description = "Inaccuracy when throwing the ender pearl.")
	public static Integer inaccuracy = 3;

	public PearlerMobs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_BE_PEARLER)
				|| mob.getPersistentData().getBoolean(HAS_ENDER_PEARL_BEEN_GIVEN))
			return;

		if (mob.getOffhandItem().isEmpty() && mob.getRandom().nextDouble() < equipEnderPearlChance)
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.ENDER_PEARL, enderPearlAmount));

		mob.getPersistentData().putBoolean(HAS_ENDER_PEARL_BEEN_GIVEN, true);
		mob.goalSelector.addGoal(2, new PearlUseGoal(mob));
	}
}
