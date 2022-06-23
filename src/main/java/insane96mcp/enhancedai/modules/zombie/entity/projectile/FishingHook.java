package insane96mcp.enhancedai.modules.zombie.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;

public class FishingHook extends Projectile {
    private final Random syncronizedRandom = new Random();
    private int outOfWaterTime;
    private static final int MAX_OUT_OF_WATER_TIME = 10;
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater = true;
    @Nullable
    private Entity hookedIn;
    private FishHookState currentState = FishHookState.FLYING;
    private final int luck;
    private final int lureSpeed;

    private FishingHook(EntityType<? extends net.minecraft.world.entity.projectile.FishingHook> p_150141_, Level p_150142_, int p_150143_, int p_150144_) {
        super(p_150141_, p_150142_);
        this.noCulling = true;
        this.luck = Math.max(0, p_150143_);
        this.lureSpeed = Math.max(0, p_150144_);
    }

    public FishingHook(EntityType<? extends net.minecraft.world.entity.projectile.FishingHook> p_150138_, Level p_150139_) {
        this(p_150138_, p_150139_, 0, 0);
    }

    public FishingHook(Player p_37106_, Level p_37107_, int p_37108_, int p_37109_) {
        this(EntityType.FISHING_BOBBER, p_37107_, p_37108_, p_37109_);
        this.setOwner(p_37106_);
        float f = p_37106_.getXRot();
        float f1 = p_37106_.getYRot();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        double d0 = p_37106_.getX() - (double)f3 * 0.3D;
        double d1 = p_37106_.getEyeY();
        double d2 = p_37106_.getZ() - (double)f2 * 0.3D;
        this.moveTo(d0, d1, d2, f1, f);
        Vec3 vec3 = new Vec3((double)(-f3), (double)Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double)(-f2));
        double d3 = vec3.length();
        vec3 = vec3.multiply(0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D, 0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D);
        this.setDeltaMovement(vec3);
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_37153_) {
        if (DATA_HOOKED_ENTITY.equals(p_37153_)) {
            int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = i > 0 ? this.level.getEntity(i - 1) : null;
        }

        super.onSyncedDataUpdated(p_37153_);
    }

    public boolean shouldRenderAtSqrDistance(double p_37125_) {
        //64d
        return p_37125_ < 4096.0D;
    }

    public void lerpTo(double p_37127_, double p_37128_, double p_37129_, float p_37130_, float p_37131_, int p_37132_, boolean p_37133_) {
    }

    public void tick() {
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level.getGameTime());
        super.tick();
        if (this.onGround) {
            ++this.life;
            if (this.life >= 1200) {
                this.discard();
                return;
            }
        }
        else {
            this.life = 0;
        }

        float f = 0.0F;
        BlockPos blockpos = this.blockPosition();
        FluidState fluidstate = this.level.getFluidState(blockpos);
        if (fluidstate.is(FluidTags.WATER)) {
            f = fluidstate.getHeight(this.level, blockpos);
        }

        boolean flag = f > 0.0F;
        if (this.currentState == FishHookState.FLYING) {
            if (this.hookedIn != null) {
                this.setDeltaMovement(Vec3.ZERO);
                this.currentState = FishHookState.HOOKED_IN_ENTITY;
                return;
            }

            if (flag) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 0.2D, 0.3D));
                //this.currentState = FishHookState.BOBBING;
                return;
            }

            this.checkCollision();
        }
        else {
            if (this.currentState == FishHookState.HOOKED_IN_ENTITY) {
                if (this.hookedIn != null) {
                    if (!this.hookedIn.isRemoved() && this.hookedIn.level.dimension() == this.level.dimension()) {
                        this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8D), this.hookedIn.getZ());
                    } else {
                        this.setHookedEntity((Entity)null);
                        this.currentState = FishHookState.FLYING;
                    }
                }

                return;
            }
        }

        if (!fluidstate.is(FluidTags.WATER)) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.updateRotation();
        if (this.currentState == FishHookState.FLYING && (this.onGround || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }

        double d1 = 0.92D;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.92D));
        this.reapplyPosition();
    }

    private void checkCollision() {
        HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.MISS || !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) this.onHit(hitresult);
    }

    protected boolean canHitEntity(Entity p_37135_) {
        return super.canHitEntity(p_37135_) || p_37135_.isAlive() && p_37135_ instanceof ItemEntity;
    }

    protected void onHitEntity(EntityHitResult p_37144_) {
        super.onHitEntity(p_37144_);
        if (!this.level.isClientSide) {
            this.setHookedEntity(p_37144_.getEntity());
        }

    }

    protected void onHitBlock(BlockHitResult p_37142_) {
        super.onHitBlock(p_37142_);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(p_37142_.distanceTo(this)));
    }

    private void setHookedEntity(@Nullable Entity p_150158_) {
        this.hookedIn = p_150158_;
        this.getEntityData().set(DATA_HOOKED_ENTITY, p_150158_ == null ? 0 : p_150158_.getId() + 1);
    }

    private void catchingFish(BlockPos p_37146_) {
        ServerLevel serverlevel = (ServerLevel)this.level;
        int i = 1;
        BlockPos blockpos = p_37146_.above();
        if (this.random.nextFloat() < 0.25F && this.level.isRainingAt(blockpos)) {
            ++i;
        }

        if (this.random.nextFloat() < 0.5F && !this.level.canSeeSky(blockpos)) {
            --i;
        }

        if (this.nibble > 0) {
            --this.nibble;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= i;
            if (this.timeUntilHooked > 0) {
                this.fishAngle += (float)(this.random.nextGaussian() * 4.0D);
                float f = this.fishAngle * ((float)Math.PI / 180F);
                float f1 = Mth.sin(f);
                float f2 = Mth.cos(f);
                double d0 = this.getX() + (double)(f1 * (float)this.timeUntilHooked * 0.1F);
                double d1 = (double)((float)Mth.floor(this.getY()) + 1.0F);
                double d2 = this.getZ() + (double)(f2 * (float)this.timeUntilHooked * 0.1F);
                BlockState blockstate = serverlevel.getBlockState(new BlockPos(d0, d1 - 1.0D, d2));
                if (serverlevel.getBlockState(new BlockPos((int)d0, (int)d1 - 1, (int)d2)).getMaterial() == net.minecraft.world.level.material.Material.WATER) {
                    if (this.random.nextFloat() < 0.15F) {
                        serverlevel.sendParticles(ParticleTypes.BUBBLE, d0, d1 - (double)0.1F, d2, 1, (double)f1, 0.1D, (double)f2, 0.0D);
                    }

                    float f3 = f1 * 0.04F;
                    float f4 = f2 * 0.04F;
                    serverlevel.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, (double)f4, 0.01D, (double)(-f3), 1.0D);
                    serverlevel.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, (double)(-f4), 0.01D, (double)f3, 1.0D);
                }
            } else {
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double d3 = this.getY() + 0.5D;
                serverlevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), d3, this.getZ(), (int)(1.0F + this.getBbWidth() * 20.0F), (double)this.getBbWidth(), 0.0D, (double)this.getBbWidth(), (double)0.2F);
                serverlevel.sendParticles(ParticleTypes.FISHING, this.getX(), d3, this.getZ(), (int)(1.0F + this.getBbWidth() * 20.0F), (double)this.getBbWidth(), 0.0D, (double)this.getBbWidth(), (double)0.2F);
                this.nibble = Mth.nextInt(this.random, 20, 40);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured -= i;
            float f5 = 0.15F;
            if (this.timeUntilLured < 20) {
                f5 += (float)(20 - this.timeUntilLured) * 0.05F;
            } else if (this.timeUntilLured < 40) {
                f5 += (float)(40 - this.timeUntilLured) * 0.02F;
            } else if (this.timeUntilLured < 60) {
                f5 += (float)(60 - this.timeUntilLured) * 0.01F;
            }

            if (this.random.nextFloat() < f5) {
                float f6 = Mth.nextFloat(this.random, 0.0F, 360.0F) * ((float)Math.PI / 180F);
                float f7 = Mth.nextFloat(this.random, 25.0F, 60.0F);
                double d4 = this.getX() + (double)(Mth.sin(f6) * f7) * 0.1D;
                double d5 = (double)((float)Mth.floor(this.getY()) + 1.0F);
                double d6 = this.getZ() + (double)(Mth.cos(f6) * f7) * 0.1D;
                BlockState blockstate1 = serverlevel.getBlockState(new BlockPos(d4, d5 - 1.0D, d6));
                if (serverlevel.getBlockState(new BlockPos(d4, d5 - 1.0D, d6)).getMaterial() == net.minecraft.world.level.material.Material.WATER) {
                    serverlevel.sendParticles(ParticleTypes.SPLASH, d4, d5, d6, 2 + this.random.nextInt(2), (double)0.1F, 0.0D, (double)0.1F, 0.0D);
                }
            }

            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        } else {
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
            this.timeUntilLured -= this.lureSpeed * 20 * 5;
        }

    }

    private boolean calculateOpenWater(BlockPos p_37159_) {
        return true;
    }

    public boolean isOpenWaterFishing() {
        return this.openWater;
    }

    public void addAdditionalSaveData(CompoundTag p_37161_) {
    }

    public void readAdditionalSaveData(CompoundTag p_37151_) {
    }

    public void handleEntityEvent(byte p_37123_) {
        if (p_37123_ == 31 && this.level.isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }

        super.handleEntityEvent(p_37123_);
    }

    protected void pullEntity(Entity p_150156_) {
        Entity entity = this.getOwner();
        if (entity != null) {
            Vec3 vec3 = (new Vec3(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ())).scale(0.1D);
            p_150156_.setDeltaMovement(p_150156_.getDeltaMovement().add(vec3));
        }
    }

    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    public void remove(Entity.RemovalReason removalReason) {
        super.remove(removalReason);
    }

    public void onClientRemoval() {

    }

    public void setOwner(@Nullable Entity p_150154_) {
        super.setOwner(p_150154_);
    }

    @Nullable
    public Entity getHookedIn() {
        return this.hookedIn;
    }

    public boolean canChangeDimensions() {
        return false;
    }

    public Packet<?> getAddEntityPacket() {
        Entity entity = this.getOwner();
        return new ClientboundAddEntityPacket(this, entity == null ? this.getId() : entity.getId());
    }

    public void recreateFromPacket(ClientboundAddEntityPacket p_150150_) {
        super.recreateFromPacket(p_150150_);
    }

    static enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        ON_GROUND;
    }
}
