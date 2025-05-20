package insane96mcp.enhancedai.modules.mobs;

import insane96mcp.enhancedai.EnhancedAI;
import insane96mcp.enhancedai.modules.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;

@Label(name = "Jump", description = "Makes mobs be able to jump in place when target is a few blocks above the mob.")
@LoadFeature(module = Modules.Ids.MOBS)
public class Jump extends Feature {
    public static final TagKey<EntityType<?>> ALLOW_JUMPING = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(EnhancedAI.MOD_ID, "allow_jumping"));

    public Jump(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob mob)
                || !mob.getType().is(ALLOW_JUMPING))
            return;

        mob.goalSelector.addGoal(1, new JumpGoal(mob));
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