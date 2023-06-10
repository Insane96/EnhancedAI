package insane96mcp.enhancedai.modules.skeleton.feature;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Blacklist;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;

@Label(name = "Wither Skeletons", description = "Wither skeletons can spawn with a bow and shoot Wither arrows.")
@LoadFeature(module = Modules.Ids.SKELETON)
public class WitherSkeletons extends Feature {

	private static final String ON_SPAWN_PROCESSED = EnhancedAI.RESOURCE_PREFIX + "wither_skeletons_on_spawn_processed";

	@Config(min = 0d, max = 1d)
	@Label(name = "Ranged chance", description = "Chance for Wither Skeletons to spawn with a bow")
	public static Double rangedChance = 0.2d;
	@Config
	@Label(name = "Wither instead of Fire", description = "Wither skeletons shoot Withered arrows instead of arrows on fire")
	public static Boolean witherInsteadOfFire = true;
	@Config
	@Label(name = "Entity Blacklist", description = "Entities that will not get affected by this feature")
	public static Blacklist entityBlacklist = new Blacklist(Collections.emptyList(), false);

	public WitherSkeletons(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| rangedChance == 0d
				|| !(event.getEntity() instanceof WitherSkeleton witherSkeleton)
				|| entityBlacklist.isEntityBlackOrNotWhitelist(witherSkeleton)
				|| witherSkeleton.getPersistentData().contains(ON_SPAWN_PROCESSED)
				|| witherSkeleton.getRandom().nextDouble() >= rangedChance)
			return;

		RandomSource randomSource = witherSkeleton.getRandom();
		float specialMultiplier = witherSkeleton.level().getCurrentDifficultyAt(witherSkeleton.blockPosition()).getSpecialMultiplier();
		//TODO AT Mob#enchantSpawnedWeapon
		witherSkeleton.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		if (randomSource.nextFloat() < 0.25F * specialMultiplier) {
			witherSkeleton.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(randomSource, witherSkeleton.getMainHandItem(), (int)(5.0F + specialMultiplier * (float)randomSource.nextInt(18)), false));
		}
		witherSkeleton.getPersistentData().putBoolean(ON_SPAWN_PROCESSED, true);
	}

	public static boolean witherInsteadOfFire() {
		return Feature.isEnabled(WitherSkeletons.class) && witherInsteadOfFire;
	}
}