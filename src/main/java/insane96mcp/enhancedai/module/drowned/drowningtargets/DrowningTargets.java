package insane96mcp.enhancedai.module.drowned.drowningtargets;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.module.EAIModules;
import insane96mcp.enhancedai.module.mobs.Spawning;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

import java.util.Optional;

@LoadFeature(module = EAIModules.MOBS, description = "Makes mobs pick up targets to drown them. Only entity types in the enhancedai:mobs/drowning_targets tag are affected by this feature.")
public class DrowningTargets extends Feature {
	public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/drowning_targets"));
    private static final ResourceLocation ATTACK_DAMAGE_ID = EnhancedAI.location("drowning_attack_damage_removal");

    public static EAIData<Boolean> DROWNING_TARGETS;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        DROWNING_TARGETS = EAIData.ofBool(this.createDataKey("drowning_targets"), (mob, drowningTargets) -> {
            if (!(mob instanceof PathfinderMob pathfinderMob))
                return;
            mob.goalSelector.removeAllGoals(goal -> goal instanceof DrownTargetGoal);
            if (drowningTargets)
                mob.goalSelector.addGoal(2, new DrownTargetGoal(pathfinderMob, 1.0D, pathfinderMob.level().getSeaLevel()));
        });
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof PathfinderMob mob)
                || !mob.getType().is(AFFECTED_ENTITY_TYPES))
            return;

        DROWNING_TARGETS.applyIfAbsent(mob, true);
    }

    @SubscribeEvent
    public void onDismount(EntityMountEvent event) {
        if (!this.isEnabled()
                || !event.isDismounting()
                || !(event.getEntityBeingMounted() instanceof Drowned drowned)
                || !event.getEntityBeingMounted().isAlive()
                || !(event.getEntityMounting() instanceof Player player)
                || player.isCreative()
                || !player.isAlive())
            return;

        event.setCanceled(true);
    }

    static class DrownTargetGoal extends Goal {
        private final PathfinderMob mob;
        private final double speedModifier;
        private final int seaLevel;

        public DrownTargetGoal(PathfinderMob pDrowned, double pSpeedModifier, int pSeaLevel) {
            this.mob = pDrowned;
            this.speedModifier = pSpeedModifier;
            this.seaLevel = pSeaLevel;
        }

        public boolean canUse() {
            return this.mob.isInWater()
                    && this.mob.getY() < (double)(this.seaLevel)
                    && this.mob.getTarget() != null
                    && this.mob.getTarget().isAlive()
                    && this.mob.getTarget().getAirSupply() <= 100
                    && this.mob.isWithinMeleeAttackRange(this.mob.getTarget())
                    && this.mob.hasLineOfSight(this.mob.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            Optional<Entity> passenger0 = this.mob.getPassengers().stream().findFirst();
            return passenger0.isPresent() && passenger0.get() == this.mob.getTarget() && passenger0.get().isAlive();
        }

        public void tick() {
            if (this.mob.getY() < (double)(this.seaLevel + 1) && (this.mob.getNavigation().isDone() || this.closeToNextPos())) {
                Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, 8, 16, new Vec3(this.mob.getX(), this.mob.level().getMinBuildHeight(), this.mob.getZ()), ((float)Math.PI / 2F));
                if (vec3 == null)
                    return;

                this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
            }
        }

        public void start() {
            if (this.mob.getTarget().isPassenger())
                this.mob.getTarget().stopRiding();
            this.mob.getTarget().startRiding(this.mob);
            MCUtils.applyModifier(this.mob, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }

        @Override
        public void stop() {
            this.mob.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_ID);
        }

        protected boolean closeToNextPos() {
            Path path = this.mob.getNavigation().getPath();
            if (path == null)
                return false;

            BlockPos blockpos = path.getTarget();
            double d0 = this.mob.distanceToSqr(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            return d0 < 4.0D;
        }
    }
}