package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SKELETON, description = "Wither skeletons can spawn with a bow and shoot Wither arrows.")
public class WitherSkeletons extends Feature {

	private static ResourceLocation ON_SPAWN_PROCESSED;

	@Config(min = 0d, max = 1d, description = "Chance for Wither Skeletons to spawn with a bow")
	public static Double rangedChance = 0.2d;
	@Config(description = "Wither skeletons shoot Withered arrows instead of arrows on fire")
	public static Boolean witherInsteadOfFire = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		ON_SPAWN_PROCESSED = this.createDataKey("on_spawn_processed");
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| rangedChance == 0d
				|| !(event.getEntity() instanceof WitherSkeleton witherSkeleton)
				|| ModNBTData.get(witherSkeleton, ON_SPAWN_PROCESSED, Boolean.class)
				|| witherSkeleton.getRandom().nextDouble() >= rangedChance)
			return;

		witherSkeleton.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
		ModNBTData.put(witherSkeleton, ON_SPAWN_PROCESSED, true);
	}

	public static boolean witherInsteadOfFire() {
		return Feature.isEnabled(WitherSkeletons.class) && witherInsteadOfFire;
	}
}