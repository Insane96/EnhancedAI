package insane96mcp.enhancedai.module.animal;

import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;

@LoadFeature(module = EAIModules.ANIMAL, description = "Makes bees less prone to wander away from their hive and get lost.")
public class BeeHiveNavigation extends Feature {
    @Config(min = 1, max = 64, description = "When wandering, bees farther than this distance from their hive will wander towards it. Vanilla is 22. Lower values keep bees closer to their hive. Should be lower than 'Hive Forget Distance'.")
    public static Integer wanderHomeDistance = 12;

    @Config(min = 16, max = 64, description = "Bees farther than this distance from their hive forget about it. Vanilla is 32. Bees also forget about their hive if its chunk is not loaded. Higher values might load more chunks.")
    public static Integer hiveForgetDistance = 48;

    /**
     * Replacement for Bee#isTooFarAway when used for the hive. Also returns true if the hive's chunk is not loaded, so the bee doesn't force load it.
     */
    public static boolean isHiveTooFarAway(Bee bee, BlockPos hivePos) {
        return !hivePos.closerThan(bee.blockPosition(), hiveForgetDistance)
                || !bee.level().isLoaded(hivePos);
    }
}
