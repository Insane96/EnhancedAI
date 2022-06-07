package insane96mcp.enhancedai.modules.witch.ai;

import insane96mcp.enhancedai.setup.Strings;
import insane96mcp.enhancedai.utils.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DarkArtWitchGoal extends Goal {
    protected Witch witch;
    protected LivingEntity target;
    protected Phase phase;
    protected int phaseTick = 0;

    protected Vec3 summonSpot;
    protected Villager villager;

    public DarkArtWitchGoal(Witch witch) {
        this.witch = witch;
        this.phase = Phase.EQUIP_EGG;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.witch.getTarget() != null && this.witch.getTarget().distanceToSqr(this.witch) < 100d && this.phase == Phase.EQUIP_EGG;
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != Phase.END;
    }

    @Override
    public void start() {
        this.target = this.witch.getTarget();
        this.witch.getPersistentData().putBoolean(Strings.Tags.Witch.PERFORMING_DARK_ARTS, true);
        this.witch.setInvulnerable(true);
        this.witch.setGlowingTag(true);
        this.witch.setDeltaMovement(0d, this.witch.getDeltaMovement().y, 0d);
    }

    @Override
    public void stop() {
        super.stop();
        this.witch.getPersistentData().putBoolean(Strings.Tags.Witch.PERFORMING_DARK_ARTS, false);
        this.witch.setInvulnerable(false);
        this.witch.setGlowingTag(false);
    }

    @Override
    public void tick() {
        this.witch.getNavigation().stop();
        this.phase.tick( this);
        this.phaseTick++;
    }

    private enum Phase {
        EQUIP_EGG {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick == 15) {
                    goal.witch.addEffect(new MobEffectInstance(MobEffects.LEVITATION, (LEVITATE_TICK - 15) * 2, 0));
                }
                if (goal.phaseTick < EQUIP_EGG_TICK) {
                    goal.witch.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.VILLAGER_SPAWN_EGG));
                    goal.witch.getLookControl().setLookAt(goal.target, 180, 180);
                }
                else {
                    goal.phase = SUMMON_VILLAGER;
                }
            }
        },
        SUMMON_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                //Tries to spawn the Villager up to 10 times
                int x = 0, y = 0, z = 0;
                for (int t = 0; t < 10; t++) {
                    float angle = goal.witch.getRandom().nextFloat() * (float) Math.PI * 2f;
                    x = (int) (Math.floor(Math.cos(angle) * 3.33f) + goal.witch.getX());
                    z = (int) (Math.floor(Math.sin(angle) * 3.33f) + goal.witch.getZ());
                    y = (int) (goal.witch.getY() + 3);

                    y = MCUtils.getYSpawn(EntityType.VILLAGER, new BlockPos(x, y, z), goal.witch.level, 4);
                    if (y != goal.witch.level.getMinBuildHeight() - 1)
                        break;
                }
                if (y < goal.witch.level.getMinBuildHeight())
                    goal.phase = END;
                else {
                    goal.summonSpot = new Vec3(x + 0.5, y, z + 0.5);
                    goal.phase = LOOK_AT_VILLAGER;
                }
                goal.witch.getLookControl().setLookAt(goal.summonSpot.x, goal.summonSpot.y, goal.summonSpot.z, 180, 180);
                goal.villager = new Villager(EntityType.VILLAGER, goal.witch.level);
                goal.villager.setPos(goal.summonSpot);
                goal.villager.getLookControl().setLookAt(goal.witch);
                goal.villager.setInvulnerable(true);
                goal.villager.setNoAi(true);
                goal.witch.level.addFreshEntity(goal.villager);
                goal.phase = LOOK_AT_VILLAGER;
            }
        },
        LOOK_AT_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick < LOOK_AT_VILLAGER_TICK) {
                    goal.witch.getLookControl().setLookAt(goal.villager, 180, 180);
                }
                else {
                    goal.phase = IMPRISON_VILLAGER;
                }
            }
        },
        IMPRISON_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick == IMPRISON_VILLAGER_TICK) {
                    goal.villager.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 2f, 0.5f);
                    goal.witch.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    goal.phase = LEVITATE;
                }
            }
        },
        LEVITATE {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick < LEVITATE_TICK) {
                    goal.witch.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 2, 1));
                    goal.witch.level.addParticle(ParticleTypes.ANGRY_VILLAGER, true, goal.witch.getX(), goal.witch.getY(), goal.witch.getZ(), 0.1, 0.1, 0.1);
                    goal.witch.getLookControl().setLookAt(goal.villager, 180, 180);
                }
                else {
                    goal.phase = LIGHTNING_STRIKE;
                }
            }
        },
        LIGHTNING_STRIKE {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick == LIGHTNING_STRIKE_TICK) {
                    LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, goal.witch.level);
                    lightningBolt.setPos(goal.villager.getPosition(1f));
                    lightningBolt.setVisualOnly(true);
                    lightningBolt.setDamage(0f);
                    goal.witch.level.addFreshEntity(lightningBolt);
                    goal.villager.setNoAi(false);
                    goal.villager.thunderHit((ServerLevel) goal.villager.level, lightningBolt);
                    goal.phase = END;
                }
            }
        },
        END {
            @Override
            public void tick(DarkArtWitchGoal goal) {}
        };

        private static final int EQUIP_EGG_TICK = 26;
        private static final int LOOK_AT_VILLAGER_TICK = EQUIP_EGG_TICK + 20;
        private static final int IMPRISON_VILLAGER_TICK = LOOK_AT_VILLAGER_TICK + 1;
        private static final int LEVITATE_TICK = IMPRISON_VILLAGER_TICK + 15;
        private static final int LIGHTNING_STRIKE_TICK = LEVITATE_TICK + 1;

        public abstract void tick(DarkArtWitchGoal goal);
    }
}
