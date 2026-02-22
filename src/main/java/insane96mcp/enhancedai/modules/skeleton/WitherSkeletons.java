package insane96mcp.enhancedai.modules.skeleton;

import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.modules.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.world.entity.monster.WitherSkeleton;

@LoadFeature(module = EAIModules.Ids.SKELETON, description = "Wither skeletons can shoot Wither arrows.")
public class WitherSkeletons extends Feature {

	@Config(description = "Wither skeletons shoot Withered arrows instead of arrows on fire")
	public static Boolean witherInsteadOfFire = true;

	public static boolean witherInsteadOfFire(WitherSkeleton witherSkeleton) {
		return Feature.isEnabled(WitherSkeletons.class) && witherInsteadOfFire && !Spawning.isUnaffectedByFeatures(witherSkeleton);
	}
}