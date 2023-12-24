package insane96mcp.enhancedai.modules.mobs.bugs.silverfish;

import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Silverfish", description = "Let them swarm. This also changes the Merge With stone goal to have 1.5 seconds cooldown before trying to merge, to prevent them from instantly getting into stone without having the chance to target someone.")
@LoadFeature(module = Modules.Ids.BUGS)
public class SilverfishFeature extends Feature {

	@Config(min = 1)
	@Label(name = "Chance to merge with stone", description = "Chance (1 in x every 2 ticks) for a silverfish to merge with a stone block. Vanilla is 10.")
	public static Integer chanceToMergeWithStone = 10;
	@Config(min = 0)
	@Label(name = "Ticks after hurt to wake up friends", description = "Vanilla is 20.")
	public static Integer ticksAfterHurtToWakeUpFriends = 10;
	@Config(min = 1)
	@Label(name = "Chance to stop waking up friends", description = "In vanilla everytime a silverfish is woken up there is 1 in 2 chance to stop waking up more silverfish. This changes the 1 in x chance.")
	public static Integer chanceToStopWakingUpFriends = 10;
	@Config(min = 1, max = 32)
	@Label(name = "Vertical Wake up Range", description = "Y range on which a hurt silverfish checks for infested stone to break. Vanilla is 5.")
	public static Integer verticalWakeUpRange = 5;
	@Config(min = 1, max = 32)
	@Label(name = "Horizontal Wake up Range", description = "XZ range on which a hurt silverfish checks for infested stone to break. Vanilla is 10.")
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
