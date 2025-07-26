package insane96mcp.enhancedai.modules.mobs.pearler;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Let mobs use ender pearls. Either put ender pearls in main or off hand and when far enough from the target they will throw it. Only mobs in the enhancedai:can_be_pearler entity type tag can be pearler.")
public class PearlerMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BE_PEARLER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_be_pearler"));
	public static final String HAS_ENDER_PEARL_BEEN_GIVEN = EnhancedAI.RESOURCE_PREFIX + "has_ender_pearl_been_given";

	@Config(min = 0d, max = 1d, description = "Chance for a mob in the entity type tag enhancedai:can_be_pearler to spawn with Ender Pearls in the offhand.\nI recommend Mobs Properties Randomness to have more control over mobs equipment as the mob will always be able to use pearls as long as it has them in the offhand.")
	public static Double equipEnderPearlChance = 0.05;
	@Config(min = 0, max = 16, description = "How many ender pearls will Mobs spawn with.")
	public static Integer enderPearlAmount = 3;
	@Config(min = 1, max = 16, description = "Inaccuracy when throwing the ender pearl.")
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
