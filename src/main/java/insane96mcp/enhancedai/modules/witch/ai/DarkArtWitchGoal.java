package insane96mcp.enhancedai.modules.witch.ai;

import insane96mcp.enhancedai.utils.MCUtils;
import net.minecraft.core.BlockPos;
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
        return this.witch.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != Phase.END;
    }

    @Override
    public void start() {
        this.target = this.witch.getTarget();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void tick() {
        this.phase.tick( this);
        this.phaseTick++;
    }

    private enum Phase {

        EQUIP_EGG {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= EQUIP_EGG_TICK) {
                    goal.witch.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.VILLAGER_SPAWN_EGG));
                }
                else {
                    goal.phase = LOOK_AT_TARGET;
                }
            }
        },
        LOOK_AT_TARGET {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= LOOK_AT_TARGET_TICK) {
                    goal.witch.getLookControl().setLookAt(goal.target);
                }
                else {
                    goal.phase = FIND_SUMMON_SPOT;
                }
            }
        },
        FIND_SUMMON_SPOT {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= FIND_SUMMON_SPOT_TICK) {
                    //Tries to spawn the Villager up to 10 times
                    int x = 0, y = 0, z = 0;
                    for (int t = 0; t < 10; t++) {
                        float angle = goal.witch.getRandom().nextFloat() * (float) Math.PI * 2f;
                        x = (int) Math.floor(Math.cos(angle) * 3.33f);
                        z = (int) Math.floor(Math.sin(angle) * 3.33f);
                        y = (int) (goal.witch.getY() + 3);

                        y = MCUtils.getYSpawn(EntityType.VILLAGER, new BlockPos(x, y, z), goal.witch.level, 4);
                        if (y != goal.witch.level.getMinBuildHeight() - 1)
                            break;
                    }
                    if (y < goal.witch.level.getMinBuildHeight())
                        goal.phase = END;
                    else {
                        goal.summonSpot = new Vec3(x + 0.5, y, z + 0.5);
                        goal.phase = SUMMON_VILLAGER;
                        goal.witch.getLookControl().setLookAt(goal.summonSpot);
                    }
                }
            }
        },
        SUMMON_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= SUMMON_VILLAGER_TICK) {
                    goal.witch.getLookControl().setLookAt(goal.summonSpot);
                    goal.villager = new Villager(EntityType.VILLAGER, goal.witch.level);
                    goal.villager.setNoAi(true);
                    goal.villager.setPos(goal.summonSpot);
                    goal.villager.getLookControl().setLookAt(goal.witch);
                    goal.witch.level.addFreshEntity(goal.villager);
                }
                else {
                    goal.phase = LOOK_AT_VILLAGER;
                }
            }
        },
        LOOK_AT_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= LOOK_AT_VILLAGER_TICK) {
                    goal.witch.getLookControl().setLookAt(goal.villager);
                }
                else {
                    goal.phase = IMPRISON_VILLAGER;
                }
            }
        },
        IMPRISON_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= IMPRISON_VILLAGER_TICK) {
                    goal.villager.playSound(SoundEvents.ANVIL_PLACE, 1f, 0.5f);
                }
                else {
                    goal.phase = LEVITATE;
                }
            }
        },
        LEVITATE {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= LEVITATE_TICK) {
                    goal.witch.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 4));
                }
                else {
                    goal.phase = LIGHTNING_STRIKE;
                }
            }
        },
        LIGHTNING_STRIKE {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick <= LIGHTNING_STRIKE_TICK) {
                    LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, goal.witch.level);
                    lightningBolt.setPos(goal.villager.getPosition(1f));
                    goal.witch.level.addFreshEntity(lightningBolt);
                }
                else {
                    goal.phase = END;
                }
            }
        },
        END {
            @Override
            public void tick(DarkArtWitchGoal goal) {}
        };

        private static final int EQUIP_EGG_TICK = 0;
        private static final int LOOK_AT_TARGET_TICK = 29;
        private static final int FIND_SUMMON_SPOT_TICK = 30;
        private static final int SUMMON_VILLAGER_TICK = 31;
        private static final int LOOK_AT_VILLAGER_TICK = SUMMON_VILLAGER_TICK + 20;
        private static final int IMPRISON_VILLAGER_TICK = LOOK_AT_VILLAGER_TICK + 1;
        private static final int LEVITATE_TICK = IMPRISON_VILLAGER_TICK + 30;
        private static final int LIGHTNING_STRIKE_TICK = LEVITATE_TICK + 1;

        public abstract void tick(DarkArtWitchGoal goal);
    }
}
