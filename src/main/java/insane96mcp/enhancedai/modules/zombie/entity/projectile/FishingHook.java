package insane96mcp.enhancedai.modules.zombie.entity.projectile;

import insane96mcp.enhancedai.setup.EAEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;

public class FishingHook extends Projectile {
    private final Random syncronizedRandom = new Random();
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private int life;
    @Nullable
    private Entity hookedIn;
    private FishHookState currentState = FishHookState.FLYING;

    public FishingHook(EntityType<? extends FishingHook> p_150141_, Level p_150142_) {
        super(p_150141_, p_150142_);
        this.noCulling = true;
    }

    public FishingHook(Entity p_37106_, Level p_37107_) {
        this(EAEntities.FISHING_HOOK.get(), p_37107_);
        this.setOwner(p_37106_);
        float xRot = p_37106_.getXRot();
        float yRot = p_37106_.getYRot();
        float cosY = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float sinY = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float cosX = -Mth.cos(-xRot * ((float)Math.PI / 180F));
        float sinX = Mth.sin(-xRot * ((float)Math.PI / 180F));
        double d0 = p_37106_.getX() - (double)sinY * 0.3D;
        double d1 = p_37106_.getEyeY();
        double d2 = p_37106_.getZ() - (double)cosY * 0.3D;
        this.moveTo(d0, d1, d2, yRot, xRot);
        Vec3 vec3 = new Vec3((-sinY), Mth.clamp(-(sinX / cosX), -5.0F, 5.0F), (-cosY));
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

        boolean isUnderwater = f > 0.0F;
        if (this.currentState == FishHookState.FLYING) {
            if (this.hookedIn != null) {
                this.setDeltaMovement(Vec3.ZERO);
                this.currentState = FishHookState.HOOKED_IN_ENTITY;
                return;
            }

            if (isUnderwater) {
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

        //Vanilla is 0.92, so this slows down slower
        this.setDeltaMovement(this.getDeltaMovement().scale(0.95D));
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

    public void addAdditionalSaveData(CompoundTag p_37161_) {
    }

    public void readAdditionalSaveData(CompoundTag p_37151_) {
    }

    public void retrieve() {
        if (this.level.isClientSide
                || this.getOwner() == null)
            return;
        if (this.hookedIn != null) {
            this.pullEntity(this.hookedIn);
            this.level.broadcastEntityEvent(this, (byte)31);
        }
        this.discard();
    }

    public void handleEntityEvent(byte p_37123_) {
        if (p_37123_ == 31 && this.level.isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }

        super.handleEntityEvent(p_37123_);
    }

    protected void pullEntity(Entity entity) {
        Entity owner = this.getOwner();
        if (owner != null) {
            Vec3 vec3 = (new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ())).scale(0.1D);
            entity.setDeltaMovement(entity.getDeltaMovement().add(vec3));
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

    public void recreateFromPacket(ClientboundAddEntityPacket p_150150_) {
        super.recreateFromPacket(p_150150_);
    }

    static enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        ON_GROUND;
    }
}
