package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.monster.WitherSkeleton;

@LoadFeature(module = Modules.Ids.SKELETON, description = "Wither skeletons can shoot Wither arrows.")
public class WitherSkeletons extends Feature {

	@Config(description = "Wither skeletons shoot Withered arrows instead of arrows on fire")
	public static Boolean witherInsteadOfFire = true;

	public static boolean witherInsteadOfFire(WitherSkeleton witherSkeleton) {
		return Feature.isEnabled(WitherSkeletons.class) && witherInsteadOfFire && !Spawning.isUnaffectedByFeatures(witherSkeleton);
	}
}