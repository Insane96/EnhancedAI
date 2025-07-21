package insane96mcp.enhancedai.modules.mobs.fisher;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Let mobs use Fishing Rods, reeling players in. Either put a Fishing Rod in main or off hand and when near enough from the target they will use it. Only mobs in enhancedai:can_be_fisher entity type tag are affected by this feature.")
public class FisherMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_BE_FISHER = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("can_be_fisher"));
    public static final TagKey<Item> FISHER_RODS = TagKey.create(Registries.ITEM, EnhancedAI.location("fisher_rods"));
	public static final String HAS_FISHING_ROD_BEEN_GIVEN = EnhancedAI.RESOURCE_PREFIX + "has_fishing_rod_been_given";

	@Config(min = 0d, max = 1d, description = "Chance for a mob in the entity type tag enhancedai:can_be_fisher to spawn with a Fishing Rod in the offhand.\nI recommend Mobs Properties Randomness to have more control over mobs equipment.")
	public static Double equipFishingRodChance = 0.07;

	@Config(min = 0d, max = 1d, description = "Chance for a fisher mob to steal an entity item in the hands instead of reeling the player.")
	public static Double hookHandsChance = 0.4d;

	@Config(description = "How fast will a mob reel in the grappled entity (or if the hook is on the ground).")
	public static Difficulty reelInTicks = new Difficulty(30, 30, 20);

	@Config(description = "How much will a mob wait before casting the bobber again. The cooldown is doubled if successfully reels in someone.")
	public static Difficulty cooldown = new Difficulty(80, 80, 60);

	public FisherMobs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_BE_FISHER))
			return;

		if (!mob.getPersistentData().getBoolean(HAS_FISHING_ROD_BEEN_GIVEN) && mob.getOffhandItem().isEmpty() && mob.getRandom().nextDouble() < equipFishingRodChance)
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.FISHING_ROD));

		mob.getPersistentData().putBoolean(HAS_FISHING_ROD_BEEN_GIVEN, true);
		mob.goalSelector.addGoal(1, new FishingTargetGoal(mob));
	}
}
