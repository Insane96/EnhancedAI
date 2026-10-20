package insane96mcp.enhancedai.module.mobs.teleporttotarget;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TeleportToTargetGoal extends Goal {
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER_ID = EnhancedAI.location("teleport_to_target");

    final Mob mob;
    final TargetingConditions targetingConditions;
    Mob toTeleport;
    int unreachableTime;
    int cooldown;

    int teleportTick = 0;

    LivingEntity actualTarget;

    public TeleportToTargetGoal(Mob mob) {
        this.mob = mob;
        this.targetingConditions = TargetingConditions.forNonCombat()
                .range(this.getFollowDistance());
        this.setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!TeleportToTarget.CAN_TELEPORT_DATA.has(this.mob)
                || this.mob.getTarget() == null
                //|| this.mob.getTarget().distanceTo(this.mob) < TeleportToTarget.MIN_DISTANCE_TO_PICK_UP.get(this.mob)
                || --this.cooldown > 0)
            return false;

        this.targetingConditions.range(this.getFollowDistance());
        TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(TeleportToTarget.CAN_TELEPORT_DATA.get(this.mob)));
        this.toTeleport = this.mob.level().getNearestEntity(
                this.mob.level().getEntitiesOfClass(Mob.class,
                        this.mob.getBoundingBox().inflate(this.getFollowDistance()),
                        living -> living.getType().is(tag)),
                this.targetingConditions,
                this.mob,
                this.mob.getX(),
                this.mob.getEyeY(),
                this.mob.getZ());
        this.actualTarget = this.mob.getTarget();
        return this.toTeleport != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.toTeleport != null && this.toTeleport.isAlive() && this.actualTarget != null && this.actualTarget.isAlive();
    }

    @Override
    public void start() {
        this.mob.getLookControl().setLookAt(this.toTeleport);
        this.mob.getNavigation().stop();
        this.mob.getNavigation().moveTo(this.toTeleport, 1.5f/*TeleportToTarget.SPEED_MODIFIER_TO_PICK_UP.get(this.mob)*/);
        this.toTeleport.getNavigation().stop();
    }

    @Override
    public void stop() {
        if (this.teleportTick > 0) {
            show(this.mob);
            if (this.toTeleport != null)
                show(this.toTeleport);
        }
        this.teleportTick = 0;
        this.toTeleport = null;
        this.unreachableTime = 0;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    public void tick() {
        if (this.teleportTick <= 0) {
            this.mob.getLookControl().setLookAt(this.toTeleport);
            this.toTeleport.getNavigation().stop();
            if (this.mob.getNavigation().isDone())
                this.mob.getNavigation().moveTo(this.toTeleport, 1.5f/*TeleportToTarget.SPEED_MODIFIER_TO_PICK_UP.get(this.mob)*/);
            if (this.mob.distanceToSqr(this.toTeleport) <= 4f) {
                hide(this.mob);
                hide(this.toTeleport);
                this.teleportTick = this.adjustedTickDelay(30);
                //this.cooldown = this.adjustedTickDelay(20);
            }
            //Only count while reaching the mob to teleport, so the goal doesn't stop while hidden
            else if (++this.unreachableTime > this.adjustedTickDelay(120)) {
                this.cooldown = this.adjustedTickDelay(300/*TeleportToTarget.COOLDOWN.get(this.mob)*/);
                this.stop();
            }
        }
        else {
            if (this.actualTarget != null && --this.teleportTick <= 0) {
                show(this.mob);
                show(this.toTeleport);
                //If the mob can't teleport, both show back where they are
                if (teleportTowards(this.mob))
                    this.teleport(this.toTeleport, this.mob.getX(), this.mob.getY(), this.mob.getZ());
                this.toTeleport.setLastHurtByMob(this.actualTarget);
                //((ServerLevel)this.toTeleport.level()).sendParticles(ParticleTypes.PORTAL, this.toTeleport.getX(), this.toTeleport.getEyeY(), this.toTeleport.getZ(), 200, 0.5, 0.5, 0.5, 0.5);
                this.cooldown = this.adjustedTickDelay(300/*TeleportToTarget.COOLDOWN.get(this.mob)*/);
                this.stop();
            }
        }
    }

    public void hide(LivingEntity entity) {
        MCUtils.applyModifier(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER_ID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
        ((ServerLevel)entity.level()).sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getEyeY(), entity.getZ(), 200, 0.5, 0.5, 0.5, 0.5);
        entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 4f, 0.5f);
        //Not using no gravity as it's saved to NBT and the mob would keep floating if unloaded while hidden
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setInvisible(true);
        //entity.setGlowingTag(true);
    }

    public void show(LivingEntity entity) {
        entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
        entity.setInvisible(false);
        entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 2f);
    }

    private boolean teleportTowards(LivingEntity entity) {
        Vec3 vec3 = new Vec3(this.actualTarget.getX() - entity.getX(), this.actualTarget.getY() - entity.getY(0.5D), this.actualTarget.getZ() - entity.getZ());
        vec3 = vec3.normalize();
        double distance = entity.distanceTo(this.actualTarget) - 2;
        double x = entity.getX() + vec3.x * distance;
        double y = entity.getY() + vec3.y * distance;
        double z = entity.getZ() + vec3.z * distance;
        return this.teleport(entity, x, y, z);
    }

    /**
     * Tries to teleport the entity to the position (on the ground), trying a few blocks above if inside a block and then randomly around it
     * @return true if the entity has been teleported
     */
    private boolean teleport(LivingEntity entity, double x, double y, double z) {
        Vec3 from = entity.position();
        for (int i = 0; i < 10; i++) {
            double tx = i < 4 ? x : x + (entity.getRandom().nextDouble() - 0.5d) * 4d;
            double ty = i < 4 ? y + i : y + entity.getRandom().nextInt(3);
            double tz = i < 4 ? z : z + (entity.getRandom().nextDouble() - 0.5d) * 4d;
            if (entity.randomTeleport(tx, ty, tz, false)) {
                entity.level().gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(entity));
                return true;
            }
        }
        return false;
    }
}
