package insane96mcp.enhancedai.modules.bugs.silverfish;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.BUGS, description = "Let them swarm. This also changes the Merge With stone goal to have 1.5 seconds cooldown before trying to merge, to prevent them from instantly getting into stone without having the chance to target someone.")
public class SilverfishFeature extends Feature {

	@Config(min = 1, description = "Chance (1 in x every 2 ticks) for a silverfish to merge with a stone block. Vanilla is 10.")
	public static Integer chanceToMergeWithStone = 10;
	@Config(min = 0, description = "Vanilla is 20.")
	public static Integer ticksAfterHurtToWakeUpFriends = 10;
	@Config(min = 1, description = "In vanilla everytime a silverfish is woken up there is 1 in 2 chance to stop waking up more silverfish. This changes the 1 in x chance.")
	public static Integer chanceToStopWakingUpFriends = 5;
	@Config(min = 1, max = 32, description = "Y range on which a hurt silverfish checks for infested stone to break. Vanilla is 5.")
	public static Integer verticalWakeUpRange = 5;
	@Config(min = 1, max = 32, description = "XZ range on which a hurt silverfish checks for infested stone to break. Vanilla is 10.")
	public static Integer horizontalWakeUpRange = 10;

	public SilverfishFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Silverfish silverfish))
			return;

		silverfish.goalSelector.removeAllGoals(goal -> goal instanceof Silverfish.SilverfishMergeWithStoneGoal);
		silverfish.goalSelector.addGoal(5, new EASilverfishMergeWithStoneGoal(silverfish));
		silverfish.goalSelector.removeAllGoals(goal -> goal instanceof Silverfish.SilverfishWakeUpFriendsGoal);
		silverfish.friendsGoal = new EASilverfishWakeUpFriendsGoal(silverfish);
		silverfish.goalSelector.addGoal(3, silverfish.friendsGoal);
	}
}
