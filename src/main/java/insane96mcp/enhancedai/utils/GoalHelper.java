package insane96mcp.enhancedai.utils;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.Optional;
import java.util.stream.Stream;

public class GoalHelper {
    public static <T extends Goal> Optional<T> getGoal(GoalSelector goalSelector, Class<T> goalClass) {
        return getGoals(goalSelector, goalClass)
                .findFirst();
    }

    public static <T extends Goal> Stream<T> getGoals(GoalSelector goalSelector, Class<T> goalClass) {
        return goalSelector.availableGoals
                .stream()
                .map(WrappedGoal::getGoal)
                .filter(goal -> goalClass.isAssignableFrom(goal.getClass()))
                .map(goalClass::cast);
    }

    public static <T extends Goal> boolean hasGoal(GoalSelector goalSelector, Class<T> goalClass) {
        return getGoal(goalSelector, goalClass).isPresent();
    }
}
