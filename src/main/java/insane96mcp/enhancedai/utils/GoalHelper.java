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

    /**
     * Wrapper for GoalSelector#removeIf stopping the goal before removal
     */
    public static boolean removeGoal(GoalSelector goalSelector, Class<? extends Goal> goalClass) {
        for (WrappedGoal wrappedGoal : goalSelector.availableGoals) {
            if (goalClass.isAssignableFrom(wrappedGoal.getGoal().getClass()))
                wrappedGoal.stop();
        }
        return goalSelector.availableGoals.removeIf(wrappedGoal -> goalClass.isAssignableFrom(wrappedGoal.getGoal().getClass()));
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
}
