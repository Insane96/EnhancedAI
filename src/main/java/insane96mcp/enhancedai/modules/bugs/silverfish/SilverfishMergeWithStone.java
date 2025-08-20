package insane96mcp.enhancedai.modules.bugs.silverfish;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.BUGS, description = "Let them swarm. This also changes the Merge With stone goal to have 1.5 seconds cooldown before trying to merge, to prevent them from instantly getting into stone without having the chance to target someone.")
public class SilverfishMergeWithStone extends Feature {

	@Config(min = 1, description = "Chance (1 in x every 2 ticks) for a silverfish to merge with a stone block. Vanilla is 10.")
	public static Integer chanceToMergeWithStone = 10;

	public static EAIData<Integer> CHANCE_TO_MERGE_WITH_STONE;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		CHANCE_TO_MERGE_WITH_STONE = EAIData.ofInt(this.createDataKey("chance_to_merge_with_stone"));
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Silverfish silverfish))
			return;

		GoalHelper.removeGoal(silverfish.goalSelector, Silverfish.SilverfishMergeWithStoneGoal.class);
		silverfish.goalSelector.addGoal(5, new EAISilverfishMergeWithStoneGoal(silverfish));

		CHANCE_TO_MERGE_WITH_STONE.applyIfAbsent(silverfish, chanceToMergeWithStone);
	}
}
