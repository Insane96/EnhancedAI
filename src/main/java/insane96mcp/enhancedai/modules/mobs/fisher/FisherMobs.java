package insane96mcp.enhancedai.modules.mobs.fisher;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Let mobs use Fishing Rods, reeling players in. Either put a Fishing Rod in main or off hand and when near enough from the target they will use it. Only mobs in enhancedai:mobs/can_equip_fishing_rod entity type tag are affected by this feature.")
public class FisherMobs extends Feature {
	public static final TagKey<EntityType<?>> CAN_EQUIP_FISHING_ROD = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_equip_fishing_rod"));
    public static final TagKey<Item> FISHER_RODS = TagKey.create(Registries.ITEM, EnhancedAI.location("valid_fisher_rods"));

	@Config(min = 0d, max = 1d, description = "Chance for entity types in the enhancedai:fisher_mobs/can_equip_rod tag to spawn with a Fishing Rod in the offhand.\nIf you require more control, use Mobs Properties Randomness")
	public static Double equipFishingRodChance = 0.07;

	@Config(min = 0d, max = 1d, description = "Chance for a fisher mob to steal an entity item in the hands instead of reeling the player.")
	public static Double hookHandsChance = 0.4d;

	@Config(description = "How fast will a mob reel in the hooked entity (or if the hook is on the ground).")
	public static Difficulty reelInTicks = new Difficulty(30, 30, 20);

	@Config(description = "How much will a mob wait before casting the bobber again. The cooldown is doubled if successfully reels in someone.")
	public static Difficulty cooldown = new Difficulty(80, 80, 60);

	@Config(min = 0d, description = "After how many ticks a mob will be forced to reel in the bobber.")
	public static Integer forceReelIn = 60;

	@Config(min = 0d, description = "Range at which the mob will stop fishing and attack the target")
	public static Double attackRange = 5d;

	@Config(min = 0d)
	public static Integer inaccuracy = 1;

	public static ResourceLocation HAS_FISHING_ROD_BEEN_GIVEN;
	public static EAIData<Double> HOOK_HANDS_CHANCE;
	public static EAIData<Integer> REEL_IN_TICKS;
	public static EAIData<Integer> COOLDOWN;
	public static EAIData<Integer> FORCE_REEL_IN;
	public static EAIData<Double> ATTACK_RANGE;
	public static EAIData<Integer> INACCURACY;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		HAS_FISHING_ROD_BEEN_GIVEN = this.createDataKey("has_fishing_rod_been_given");
		HOOK_HANDS_CHANCE = EAIData.ofDouble(this.createDataKey("hook_hands_chance"));
		REEL_IN_TICKS = EAIData.ofInt(this.createDataKey("reel_in_ticks"));
		COOLDOWN = EAIData.ofInt(this.createDataKey("cooldown"));
		FORCE_REEL_IN = EAIData.ofInt(this.createDataKey("force_reel_in"));
		ATTACK_RANGE = EAIData.ofDouble(this.createDataKey("attack_range"));
		INACCURACY = EAIData.ofInt(this.createDataKey("inaccuracy"));
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.getType().is(CAN_EQUIP_FISHING_ROD))
			return;

		if (!ModNBTData.get(mob, HAS_FISHING_ROD_BEEN_GIVEN, Boolean.class) && mob.getOffhandItem().isEmpty() && mob.getRandom().nextDouble() < equipFishingRodChance)
			mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.FISHING_ROD));

		ModNBTData.put(mob, HAS_FISHING_ROD_BEEN_GIVEN, true);
		mob.goalSelector.addGoal(1, new FishingTargetGoal(mob));
		HOOK_HANDS_CHANCE.applyIfAbsent(mob, hookHandsChance);
		REEL_IN_TICKS.applyIfAbsent(mob, (int) reelInTicks.getByDifficulty(mob.level()));
		COOLDOWN.applyIfAbsent(mob, (int) cooldown.getByDifficulty(mob.level()));
		FORCE_REEL_IN.applyIfAbsent(mob, forceReelIn);
		ATTACK_RANGE.applyIfAbsent(mob, attackRange);
		INACCURACY.applyIfAbsent(mob, inaccuracy);
	}
}
