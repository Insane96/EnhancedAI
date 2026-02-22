package insane96mcp.enhancedai.ai;

import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.insanelib.data.ObjTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EAIAvoidEntityGoal<T extends LivingEntity> extends Goal {
    protected final PathfinderMob goalOwner;
    protected final Class<T> classToAvoid;
    protected final EAIData<Integer> avoidDistanceFar;
    protected final EAIData<Integer> avoidDistanceNear;
    protected final EAIData<Double> farSpeed;
    protected final EAIData<Double> nearSpeed;
    protected final EAIData<Integer> horizontalDistance;
    protected final EAIData<Integer> verticalDistance;

    private final TargetingConditions builtTargetSelector;

    protected T avoidTarget;
    protected Path path;

    protected EAIAvoidEntityGoal(Builder<T> builder) {
        this.goalOwner = builder.goalOwner;
        this.classToAvoid = builder.classToAvoid;
        this.avoidDistanceFar = builder.avoidDistanceFar;
        this.avoidDistanceNear = builder.avoidDistanceNear;
        this.farSpeed = builder.farSpeed;
        this.nearSpeed = builder.nearSpeed;
        this.horizontalDistance = builder.horizontalDistance;
        this.verticalDistance = builder.verticalDistance;

        Predicate<LivingEntity> predicate = builder.predicate
                .and(EntitySelector.NO_CREATIVE_OR_SPECTATOR);

        if (builder.objTag != null) {
            predicate = predicate.and(builder.objTag::matchesEntity);
        }

        if (builder.entityTypesToAvoid != null) {
            predicate = predicate.and(e -> e.getType().is(builder.entityTypesToAvoid));
        }

        this.builtTargetSelector = TargetingConditions.forCombat()
                .range(this.avoidDistanceFar.get(this.goalOwner))
                .selector(predicate);
    }

    @Override
    public boolean canUse() {
        this.avoidTarget = this.goalOwner.level().getNearestEntity(
                this.classToAvoid,
                this.builtTargetSelector,
                this.goalOwner,
                this.goalOwner.getX(),
                this.goalOwner.getY(),
                this.goalOwner.getZ(),
                this.goalOwner.getBoundingBox().inflate(this.avoidDistanceFar.get(this.goalOwner))
        );

        if (this.avoidTarget == null)
            return false;

        Vec3 away = DefaultRandomPos.getPosAway(
                this.goalOwner,
                this.horizontalDistance.get(this.goalOwner),
                this.verticalDistance.get(this.goalOwner),
                this.avoidTarget.position()
        );
        if (away == null)
            return false;
        if (this.avoidTarget.distanceToSqr(away.x, away.y, away.z) < this.avoidTarget.distanceToSqr(this.goalOwner))
            return false;

        this.path = this.goalOwner.getNavigation().createPath(away.x, away.y, away.z, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.goalOwner.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.goalOwner.getNavigation().moveTo(this.path, this.farSpeed.get(this.goalOwner));
    }

    @Override
    public void stop() {
        this.avoidTarget = null;
    }

    @Override
    public void tick() {
        int near = this.avoidDistanceNear.get(this.goalOwner);
        if (this.goalOwner.distanceToSqr(this.avoidTarget) < near * near) {
            this.goalOwner.getNavigation().setSpeedModifier(this.nearSpeed.get(this.goalOwner));
        } else {
            this.goalOwner.getNavigation().setSpeedModifier(this.farSpeed.get(this.goalOwner));
        }
    }

    public static class Builder<T extends LivingEntity> {
        private final PathfinderMob goalOwner;
        private final Class<T> classToAvoid;
        private final EAIData<Integer> avoidDistanceFar;
        private final EAIData<Integer> avoidDistanceNear;
        private final EAIData<Double> farSpeed;
        private final EAIData<Double> nearSpeed;
        private final EAIData<Integer> horizontalDistance;
        private final EAIData<Integer> verticalDistance;

        private Predicate<LivingEntity> predicate = e -> true;
        private @Nullable ObjTag<EntityType<?>> objTag = null;
        private @Nullable TagKey<EntityType<?>> entityTypesToAvoid = null;

        public Builder(PathfinderMob goalOwner, Class<T> classToAvoid,
                       EAIData<Integer> avoidDistanceFar, EAIData<Integer> avoidDistanceNear,
                       EAIData<Double> farSpeed, EAIData<Double> nearSpeed, EAIData<Integer> horizontalDistance, EAIData<Integer> verticalDistance) {
            this.goalOwner = goalOwner;
            this.classToAvoid = classToAvoid;
            this.avoidDistanceFar = avoidDistanceFar;
            this.avoidDistanceNear = avoidDistanceNear;
            this.farSpeed = farSpeed;
            this.nearSpeed = nearSpeed;
            this.horizontalDistance = horizontalDistance;
            this.verticalDistance = verticalDistance;
        }

        public Builder<T> withPredicate(Predicate<LivingEntity> predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder<T> withObjTag(ObjTag<EntityType<?>> matcher) {
            this.objTag = matcher;
            return this;
        }

        public Builder<T> withTagToAvoid(TagKey<EntityType<?>> tag) {
            this.entityTypesToAvoid = tag;
            return this;
        }

        public EAIAvoidEntityGoal<T> build() {
            return new EAIAvoidEntityGoal<>(this);
        }
    }
}

