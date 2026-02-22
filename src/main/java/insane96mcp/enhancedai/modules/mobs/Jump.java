package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.data.EAIData;
import insane96mcp.enhancedai.modules.EAIModules;
import insane96mcp.enhancedai.utils.GoalHelper;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.EnumSet;

@LoadFeature(module = EAIModules.Ids.MOBS, description = "Makes mobs be able to jump in place when their target is a few blocks above them. Only entity types in the `enhancedai:mobs/can_jump_in_place` tag can jump")
public class Jump extends Feature {
    public static final TagKey<EntityType<?>> CAN_JUMP = TagKey.create(Registries.ENTITY_TYPE, EnhancedAI.location("mobs/can_jump_in_place"));

	public static EAIData<Boolean> CAN_JUMP_DATA;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
		CAN_JUMP_DATA = EAIData.ofBool(this.createDataKey("can_jump"), (mob, canJump) -> {
			GoalHelper.removeGoal(mob.goalSelector, JumpGoal.class);
			if (canJump)
				mob.goalSelector.addGoal(1, new JumpGoal(mob));
		});
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || Spawning.isUnaffectedByFeatures(event.getEntity())
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(CAN_JUMP))
            return;

		CAN_JUMP_DATA.applyIfAbsent(mob, true);
    }

    public static class JumpGoal extends Goal {
        protected LivingEntity target;
        protected Mob goalOwner;
        protected int ticksWithoutPath;

        public JumpGoal(Mob goalOwner) {
            super();
            this.goalOwner = goalOwner;
            this.setFlags(EnumSet.of(Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!this.goalOwner.onGround())
                return false;
            this.target = this.goalOwner.getTarget();
            if (this.target == null)
                return false;
            if (this.goalOwner.getNavigation().isDone() || this.goalOwner.getNavigation().isStuck())
                ticksWithoutPath++;
            else {
                ticksWithoutPath = 0;
                return false;
            }
            double yDistance = this.target.getY() - this.goalOwner.getY();
            double x = target.getX() - this.goalOwner.getX();
            double z = target.getZ() - this.goalOwner.getZ();
            double xzDistance = x * x + z * z;
            MobEffectInstance jumpBoost = this.goalOwner.getEffect(MobEffects.JUMP);
            double bonusJumpBoost = 0;
            if (jumpBoost != null)
                bonusJumpBoost = (jumpBoost.getAmplifier() + 1) * 0.75f;
            return xzDistance < 8 && yDistance > 0 && yDistance <= 1 + bonusJumpBoost + Mth.ceil(this.goalOwner.getBbHeight()) && ticksWithoutPath > adjustedTickDelay(25);
        }

        @Override
        public void stop() {
            this.ticksWithoutPath = 0;
        }

        @Override
        public void start() {
            this.goalOwner.getJumpControl().jump();
            this.stop();
        }
    }
}