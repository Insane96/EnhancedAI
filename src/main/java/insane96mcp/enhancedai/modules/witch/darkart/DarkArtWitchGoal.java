package insane96mcp.enhancedai.modules.witch.darkart;

import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class DarkArtWitchGoal extends Goal {
    protected final Mob mob;
    protected LivingEntity target;
    protected Phase phase;
    protected int phaseTick = 0;

    protected Vec3 summonSpot;
    protected Villager villager;

    public DarkArtWitchGoal(Mob mob) {
        this.mob = mob;
        this.phase = Phase.EQUIP_EGG;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null
                && !(this.mob.getTarget() instanceof Raider)
                && this.mob.getTarget().distanceToSqr(this.mob) < DarkArt.triggerDistance * DarkArt.triggerDistance
                && this.mob.getSensing().hasLineOfSight(this.mob.getTarget())
                && this.phase == Phase.EQUIP_EGG;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getTarget() != null && this.phase != Phase.END && (this.summonSpot == null || this.mob.distanceToSqr(this.summonSpot) <= DarkArt.cancelDistance * DarkArt.cancelDistance);
    }

    @Override
    public void start() {
        this.target = this.mob.getTarget();
		ModNBTData.put(this.mob, DarkArt.PERFORMING_DARK_ARTS, true);
        this.mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
        this.mob.setGlowingTag(true);
        this.mob.setDeltaMovement(0d, this.mob.getDeltaMovement().y, 0d);
    }

    @Override
    public void stop() {
		ModNBTData.put(this.mob, DarkArt.PERFORMING_DARK_ARTS, false);
        this.mob.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        this.mob.removeEffect(MobEffects.LEVITATION);
        this.mob.setGlowingTag(false);
        this.summonSpot = null;
		this.forceStop();
    }

    @Override
    public void tick() {
        this.mob.getNavigation().stop();
        this.phase.tick(this);
        this.phaseTick++;
    }

    public boolean isRunning() {
        return this.phaseTick > 0;
    }

    public void forceStop() {
        if (this.villager != null && !this.villager.isRemoved())
            this.villager.discard();
    }

    public enum Phase {
        EQUIP_EGG {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick == 20) {
                    goal.mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, (LEVITATE_TICK - 20) * 2, 0));
                }

                if (goal.phaseTick < EQUIP_EGG_TICK) {
                    goal.mob.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.VILLAGER_SPAWN_EGG));
					goal.mob.setDropChance(EquipmentSlot.MAINHAND, -2f);
                    goal.mob.getLookControl().setLookAt(goal.target, 180, 180);
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
                    float angle = goal.mob.getRandom().nextFloat() * (float) Math.PI * 2f;
                    x = (int) (Math.floor(Math.cos(angle) * 3.33f) + goal.mob.getX());
                    z = (int) (Math.floor(Math.sin(angle) * 3.33f) + goal.mob.getZ());
                    y = (int) (goal.mob.getY() + 3);

                    y = MCUtils.getFittingY(EntityType.VILLAGER, new BlockPos(x, y, z), goal.mob.level(), 6);
                    if (y != goal.mob.level().getMinBuildHeight() - 1)
                        break;
                }
                if (y < goal.mob.level().getMinBuildHeight()) {
                    goal.phase = END;
                    return;
                }
                else {
                    goal.summonSpot = new Vec3(x + 0.5, y, z + 0.5);
                    goal.phase = LOOK_AT_VILLAGER;
                }
                goal.mob.getLookControl().setLookAt(goal.summonSpot.x, goal.summonSpot.y, goal.summonSpot.z, 180, 180);
                goal.villager = new Villager(EntityType.VILLAGER, goal.mob.level());
                goal.villager.setPos(goal.summonSpot);
                goal.villager.getLookControl().setLookAt(goal.mob);
                goal.villager.setInvulnerable(true);
                goal.villager.setNoAi(true);
                goal.mob.level().addFreshEntity(goal.villager);
                goal.phase = LOOK_AT_VILLAGER;
            }
        },
        LOOK_AT_VILLAGER {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick < LOOK_AT_VILLAGER_TICK) {
                    goal.mob.getLookControl().setLookAt(goal.villager, 180, 180);
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
                    goal.mob.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
					goal.mob.setDropChance(EquipmentSlot.MAINHAND, 0.085f);
                    goal.phase = LEVITATE;
                }
            }
        },
        LEVITATE {
            @Override
            public void tick(DarkArtWitchGoal goal) {
                if (goal.phaseTick < LEVITATE_TICK) {
                    goal.mob.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 2, 1));
                    goal.mob.level().addParticle(ParticleTypes.ANGRY_VILLAGER, true, goal.mob.getX(), goal.mob.getY(), goal.mob.getZ(), 0.1, 0.1, 0.1);
                    goal.mob.getLookControl().setLookAt(goal.villager, 180, 180);
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
                    LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, goal.mob.level());
                    lightningBolt.setPos(goal.villager.getPosition(1f));
                    lightningBolt.setVisualOnly(true);
                    lightningBolt.setDamage(0f);
                    goal.mob.level().addFreshEntity(lightningBolt);
                    goal.summonWitch();
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

    private void summonWitch() {
        ServerLevel serverLevel = (ServerLevel) this.mob.level();
        Witch witch = EntityType.WITCH.create(serverLevel);
		if (witch != null) {
			witch.moveTo(this.villager.getX(), this.villager.getY(), this.villager.getZ(), this.villager.getYRot(), this.villager.getXRot());
			witch.finalizeSpawn(serverLevel, this.mob.level().getCurrentDifficultyAt(witch.blockPosition()), MobSpawnType.CONVERSION, null, null);
			serverLevel.addFreshEntityWithPassengers(witch);
			if (!DarkArt.summonedWitchesCanBeDarkArt)
				DarkArt.DARK_ARTS.apply(witch, false);
		}
        this.villager.discard();
    }
}
