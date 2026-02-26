package insane96mcp.enhancedai.module.bugs.silverfish.wakeupfriends;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import net.minecraft.world.entity.monster.Silverfish;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@LoadFeature(module = EAIModules.BUGS, description = "Let them swarm.")
public class SilverfishWakeUpFriends extends Feature {

	@Config(min = 0, description = "Vanilla is 20.")
	public static Integer ticksAfterHurtToWakeUpFriends = 10;
	@Config(min = 1, description = "In vanilla everytime a silverfish is woken up there is 1 in 2 chance to stop waking up more silverfish. This changes the 1 in x chance.")
	public static Integer chanceToStopWakingUpFriends = 5;
	@Config(min = 1, max = 32, description = "Y range on which a hurt silverfish checks for infested stone to break. Vanilla is 5.")
	public static Integer verticalWakeUpRange = 5;
	@Config(min = 1, max = 32, description = "XZ range on which a hurt silverfish checks for infested stone to break. Vanilla is 10.")
	public static Integer horizontalWakeUpRange = 10;

	public static EAIData<Integer> TICKS_AFTER_HURT_TO_WAKE_UP_FRIENDS;
	public static EAIData<Integer> CHANCE_TO_STOP_WAKING_UP_FRIENDS;
	public static EAIData<Integer> VERTICAL_WAKE_UP_RANGE;
	public static EAIData<Integer> HORIZONTAL_WAKE_UP_RANGE;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		TICKS_AFTER_HURT_TO_WAKE_UP_FRIENDS = EAIData.ofInt(this.createDataKey("ticks_after_hurt_to_wake_up_friends"));
		CHANCE_TO_STOP_WAKING_UP_FRIENDS = EAIData.ofInt(this.createDataKey("chance_to_stop_waking_up_friends"));
		VERTICAL_WAKE_UP_RANGE = EAIData.ofInt(this.createDataKey("vertical_wake_up_range"));
		HORIZONTAL_WAKE_UP_RANGE = EAIData.ofInt(this.createDataKey("horizontal_wake_up_range"));
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
				|| event.getLevel().isClientSide
				|| !(event.getEntity() instanceof Silverfish silverfish))
			return;

		GoalHelper.removeGoal(silverfish.goalSelector, Silverfish.SilverfishWakeUpFriendsGoal.class);
		silverfish.friendsGoal = new EAISilverfishWakeUpFriendsGoal(silverfish);
		silverfish.goalSelector.addGoal(3, silverfish.friendsGoal);

		TICKS_AFTER_HURT_TO_WAKE_UP_FRIENDS.applyIfAbsent(silverfish, ticksAfterHurtToWakeUpFriends);
		CHANCE_TO_STOP_WAKING_UP_FRIENDS.applyIfAbsent(silverfish, chanceToStopWakingUpFriends);
		VERTICAL_WAKE_UP_RANGE.applyIfAbsent(silverfish, verticalWakeUpRange);
		HORIZONTAL_WAKE_UP_RANGE.applyIfAbsent(silverfish, horizontalWakeUpRange);
	}
}
