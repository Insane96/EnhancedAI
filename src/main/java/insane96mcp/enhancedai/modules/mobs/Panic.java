package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;

@LoadFeature(module = Modules.Ids.MOBS, description = "Makes mobs panic when on fire.")
public class Panic extends Feature {
    public static final TagKey<EntityType<?>> AFFECTED_ENTITY_TYPES = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/panic_on_fire"));

    @Config(min = 0d, max = 1d, description = "Chance for a mob to get the panic on fire AI")
    public static Double chance = 0.8d;

    public static EAIData<Boolean> PANIC_ON_FIRE;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        PANIC_ON_FIRE = EAIData.ofBool(this.createDataKey("panic_on_fire"), (mob, panicOnFire) -> {
            if (!(mob instanceof PathfinderMob pMob))
                return;
            if (panicOnFire)
                mob.goalSelector.addGoal(1, new EAIPanicGoal(pMob, 1.25));
        });
    }

    @SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
                || !(event.getEntity() instanceof PathfinderMob mob)
                || !event.getEntity().getType().is(AFFECTED_ENTITY_TYPES))
			return;

        PANIC_ON_FIRE.applyIfAbsent(mob, mob.getRandom().nextDouble() < chance);
	}

    public static class EAIPanicGoal extends Goal {
        protected final PathfinderMob mob;
        protected final double speedModifier;
        protected double posX;
        protected double posY;
        protected double posZ;
        protected boolean isRunning;

        public EAIPanicGoal(PathfinderMob pMob, double pSpeedModifier) {
            this.mob = pMob;
            this.speedModifier = pSpeedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            if (!this.mob.isOnFire())
                return false;

            BlockPos blockpos = this.lookForWater(this.mob.level(), this.mob, 5);
            if (blockpos != null) {
                this.posX = blockpos.getX();
                this.posY = blockpos.getY();
                this.posZ = blockpos.getZ();
                return true;
            }

            return this.findRandomPosition();
        }

        protected boolean findRandomPosition() {
            Vec3 vec3 = DefaultRandomPos.getPos(this.mob, 5, 4);
            if (vec3 == null) {
                return false;
            } else {
                this.posX = vec3.x;
                this.posY = vec3.y;
                this.posZ = vec3.z;
                return true;
            }
        }

        public boolean isRunning() {
            return this.isRunning;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            this.isRunning = true;
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.isRunning = false;
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @Nullable
        protected BlockPos lookForWater(BlockGetter pLevel, Entity pEntity, int pRange) {
            BlockPos blockpos = pEntity.blockPosition();
            return !pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos).isEmpty()
                    ? null
                    : BlockPos.findClosestMatch(pEntity.blockPosition(), pRange, 1, (p_196649_) -> pLevel.getFluidState(p_196649_).is(FluidTags.WATER)).orElse(null);
        }
    }
}
