package insane96mcp.enhancedai.utils;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Optional;

public class GoalHelper {
    public static <T extends Goal> Optional<T> getGoal(Mob mob, Class<T> goalClass) {
return mob.goalSelector.availableGoals
.stream()
.map(WrappedGoal::getGoal)
                .filter(goal -> goalClass.isAssignableFrom(goal.getClass()))
                .map(goalClass::cast)
                .findFirst();
    }
}
