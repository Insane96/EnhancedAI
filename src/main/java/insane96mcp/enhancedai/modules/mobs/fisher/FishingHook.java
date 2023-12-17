package insane96mcp.enhancedai.modules.mobs.fisher;

import insane96mcp.enhancedai.setup.EAEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FishingHook extends Projectile {
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
    }

    protected void defineSynchedData() {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_37153_) {
        if (DATA_HOOKED_ENTITY.equals(p_37153_)) {
            int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = i > 0 ? this.level().getEntity(i - 1) : null;
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
        super.tick();
        if (this.getOwner() == null || !this.getOwner().isAlive()) {
            this.discard();
            return;
        }
        if (this.distanceToSqr(this.getOwner()) > 1024d) {
            this.discard();
        }
        if (this.onGround()) {
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
        FluidState fluidstate = this.level().getFluidState(blockpos);
        if (fluidstate.is(FluidTags.WATER)) {
            f = fluidstate.getHeight(this.level(), blockpos);
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
                    if (!this.hookedIn.isRemoved() && this.hookedIn.level().dimension() == this.level().dimension()) {
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
        if (this.currentState == FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }

        //Vanilla is 0.92, so this slows down slower
        this.setDeltaMovement(this.getDeltaMovement().scale(0.96D));
        this.reapplyPosition();
    }

    private void checkCollision() {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.MISS || !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) this.onHit(hitresult);
    }

    protected boolean canHitEntity(Entity p_37135_) {
        return super.canHitEntity(p_37135_) || p_37135_.isAlive() && p_37135_ instanceof ItemEntity;
    }

    protected void onHitEntity(EntityHitResult p_37144_) {
        super.onHitEntity(p_37144_);
        if (!this.level().isClientSide) {
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

    public void retrieve(boolean isInventoryHooked) {
        if (this.level().isClientSide
                || this.getOwner() == null)
            return;
        if (this.hookedIn != null) {
            if (isInventoryHooked) {
                if (this.hookedIn instanceof Player player && !player.getInventory().isEmpty()) {
                    int slot;
                    ItemStack itemStack;
                    int blowUpPrevention = 64;
                    do {
                        slot = this.random.nextInt(36);
                        itemStack = player.getInventory().getItem(slot);
                        if (--blowUpPrevention <= 0)
                            break;
                    } while (itemStack.isEmpty());
                    if (!itemStack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(this.level(), this.hookedIn.position().x, this.hookedIn.getEyeY(), this.hookedIn.position().z, itemStack.copy(), 0, 0, 0);
                        itemEntity.setDefaultPickUpDelay();
                        this.pullEntity(itemEntity);
                        this.level().addFreshEntity(itemEntity);
                        player.getInventory().removeItem(slot, 256);
                    }
                }
                else if (this.hookedIn instanceof LivingEntity livingEntity){
                    //TODO Steal in hand or armor items
                    //livingEntity.getItemBySlot(EquipmentSlot.)
                }
            }
            else {
                this.pullEntity(this.hookedIn);
                this.level().broadcastEntityEvent(this, EntityEvent.FISHING_ROD_REEL_IN);
            }
        }
        this.discard();
    }

    public void handleEntityEvent(byte p_37123_) {
        if (p_37123_ == 31 && this.level().isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }

        super.handleEntityEvent(p_37123_);
    }

    protected void pullEntity(Entity entity) {
        Entity owner = this.getOwner();
        if (owner != null) {
            Vec3 vec3 = (new Vec3(owner.getX() - this.getX(), Math.max(owner.getY() - this.getY(), 1d), owner.getZ() - this.getZ())).scale(entity instanceof LivingEntity ? 0.3D : 0.1d);
            entity.stopRiding();
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

    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket p_150150_) {
        super.recreateFromPacket(p_150150_);
    }

    enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        ON_GROUND
    }
}
