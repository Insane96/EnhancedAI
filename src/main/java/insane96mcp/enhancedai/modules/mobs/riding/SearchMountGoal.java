package insane96mcp.enhancedai.modules.mobs.riding;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class SearchMountGoal extends Goal {
    final Mob mob;
    final TargetingConditions targetingConditions;
    LivingEntity mount;
    int unreachableTime;
    int cooldown;
    public SearchMountGoal(Mob mob) {
        this.mob = mob;
        this.targetingConditions = TargetingConditions.forNonCombat()
				.range(this.getFollowDistance())
				.selector(livingEntity -> !livingEntity.isVehicle() && livingEntity.onGround());
        this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Riding.CAN_MOUNT_DATA.has(this.mob)
				|| this.mob.getVehicle() != null
				|| --this.cooldown > 0)
            return false;

		this.targetingConditions.range(this.getFollowDistance());
		TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(Riding.CAN_MOUNT_DATA.get(this.mob)));
        this.mount = this.mob.level().getNearestEntity(
				this.mob.level().getEntitiesOfClass(LivingEntity.class,
						this.mob.getBoundingBox().inflate(this.getFollowDistance()),
						living -> living.getType().is(tag)),
				this.targetingConditions,
				this.mob,
				this.mob.getX(),
				this.mob.getEyeY(),
				this.mob.getZ());
        return this.mount != null;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.mount = null;
        this.unreachableTime = 0;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.mount);
        if (this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().moveTo(this.mount, 1d);
        }
        if (this.mob.distanceToSqr(this.mount) <= 3f) {
            this.mob.startRiding(this.mount, false);
            this.stop();
        }
        if (++this.unreachableTime > this.adjustedTickDelay(200)) {
            this.cooldown = this.adjustedTickDelay(200);
            this.stop();
        }
    }
}
