package insane96mcp.enhancedai.utils;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import org.apache.commons.lang3.NotImplementedException;

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

    public static <T extends Goal> Optional<T> removeGoal(GoalSelector goalSelector, Class<T> goalClass) {
        Optional<WrappedGoal> wrappedGoalToRemove = findFirstMatchingGoal(goalSelector, goalClass);

        wrappedGoalToRemove.ifPresent(wrappedGoal -> {
            wrappedGoal.stop();
            goalSelector.removeGoal(wrappedGoal.getGoal());
        });

        return wrappedGoalToRemove.map(wrapped -> goalClass.cast(wrapped.getGoal()));
    }

    private static <T extends Goal> Optional<WrappedGoal> findFirstMatchingGoal(GoalSelector goalSelector, Class<T> goalClass) {
        return goalSelector.availableGoals
                .stream()
                .filter(wrappedGoal -> goalClass.isAssignableFrom(wrappedGoal.getGoal().getClass()))
                .findFirst();
    }

    /**
     * Wrapper for GoalSelector#addGoal shifting existing goals if necessary
     */
    public static boolean insertGoal(GoalSelector goalSelector, Goal goal, int priority) {
        throw new NotImplementedException();
    }

    public static <T extends Goal> boolean hasGoal(GoalSelector goalSelector, Class<T> goalClass) {
        return getGoal(goalSelector, goalClass).isPresent();
    }

    public static <T extends Goal> boolean hasGoal(GoalSelector goalSelector, Goal goal) {
        for (WrappedGoal wrappedGoal : goalSelector.availableGoals) {
            if (wrappedGoal.getGoal() == goal)
                return true;
        }
        return false;
    }

    public static <T extends Goal> boolean isRunning(GoalSelector goalSelector, Class<T> goalClass) {
        return goalSelector.getRunningGoals().anyMatch(goal -> goalClass.isAssignableFrom(goal.getClass()));
    }
}
