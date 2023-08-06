package com.mod.entity;

import com.google.common.collect.Lists;
import com.mod.registry.EntityRegister;
import com.mod.registry.ItemRegister;
import com.mod.registry.SoundRegister;
import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.mod.entity.ExtendedBoatEntityType.WARPED;

public class ExtraBoatEntity extends Entity implements VariantHolder<BoatEntity.Type> {
    private static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> BOAT_TYPE = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> LEFT_PADDLE_MOVING = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RIGHT_PADDLE_MOVING = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> BUBBLE_WOBBLE_TICKS = DataTracker.registerData(ExtraBoatEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final float NEXT_PADDLE_PHASE = 0.3926991f;
    /**
     * A boat will emit a sound event every time a paddle is near this rotation.
     */
    public static final double EMIT_SOUND_EVENT_PADDLE_ROTATION = 0.7853981852531433;
    private final float[] paddlePhases = new float[2];
    private float ticksUnderlava;
    private float yawVelocity;
    private int field_7708;
    private double x;
    private double y;
    private double z;
    private double boatYaw;
    private double boatPitch;
    private boolean pressingLeft;
    private boolean pressingRight;
    private boolean pressingForward;
    private boolean pressingBack;
    private double lavaLevel;
    private float nearbySlipperiness;
    private Location location;
    private Location lastLocation;
    private double fallVelocity;
    private boolean onBubbleColumnSurface;
    private boolean bubbleColumnIsDrag;
    private float bubbleWobbleStrength;
    private float bubbleWobble;
    private float lastBubbleWobble;

    public ExtraBoatEntity(EntityType<? extends ExtraBoatEntity> entityType, World world) {
        super(entityType, world);
        this.intersectionChecked = true;
    }

    public ExtraBoatEntity(World world, double x, double y, double z) {
        this(EntityRegister.EXTRA_BOAT, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
        this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
        this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0f);
        this.dataTracker.startTracking(BOAT_TYPE, BoatEntity.Type.OAK.ordinal());
        this.dataTracker.startTracking(LEFT_PADDLE_MOVING, false);
        this.dataTracker.startTracking(RIGHT_PADDLE_MOVING, false);
        this.dataTracker.startTracking(BUBBLE_WOBBLE_TICKS, 0);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    @Override
    public double getMountedHeightOffset() {
        return -0.1;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean attackingPlayerInCreative;
        if (this.isInvulnerableTo(source))
            return false;
        if (isClient() || this.isRemoved())
            return true;
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0f);
        this.scheduleVelocityUpdate();
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
        attackingPlayerInCreative = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).getAbilities().creativeMode;
        if (attackingPlayerInCreative || this.getDamageWobbleStrength() > 40.0f) {
            if (!attackingPlayerInCreative && this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
                this.dropItems(source);
            this.discard();
        }
        return true;
    }

    protected void dropItems(DamageSource source) {
        this.dropItem(this.asItem());
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag) {
        if (!isClient()) {
            this.onBubbleColumnSurface = true;
            this.bubbleColumnIsDrag = drag;
            if (this.getBubbleWobbleTicks() == 0) {
                this.setBubbleWobbleTicks(60);
            }
        }
        this.getWorld().addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
        if (this.random.nextInt(20) == 0) {
            this.getWorld().playSoundFromEntity(null, this, this.getSplashSound(), this.getSoundCategory(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
            this.emitGameEvent(GameEvent.SPLASH, this.getControllingPassenger());
        }
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (entity instanceof ExtraBoatEntity) {
            if (entity.getBoundingBox().minY < this.getBoundingBox().maxY)
                super.pushAwayFrom(entity);
        } else if (entity.getBoundingBox().minY <= this.getBoundingBox().minY)
            super.pushAwayFrom(entity);
    }

    public Item asItem() {
        if (this.getVariant() == WARPED) return ItemRegister.WARPED_BOAT;
        return ItemRegister.CRIMSON_BOAT;
    }

    @Override
    public void animateDamage(float yaw) {
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0f);
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.boatYaw = yaw;
        this.boatPitch = pitch;
        this.field_7708 = 10;
    }

    @Override
    public Direction getMovementDirection() {
        return this.getHorizontalFacing().rotateYClockwise();
    }

    @Override
    public void tick() {
        this.lastLocation = this.location;
        this.location = this.checkLocation();
        this.ticksUnderlava = this.location == Location.UNDER_LAVA || this.location == Location.UNDER_FLOWING_LAVA ? this.ticksUnderlava + 1.0f : 0.0f;
        if (!isClient() && this.ticksUnderlava >= 60.0f)
            this.removeAllPassengers();
        if (this.getDamageWobbleTicks() > 0)
            this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
        if (this.getDamageWobbleStrength() > 0.0f)
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0f);
        super.tick();
        this.updatePositionAndRotation();
        if (this.isLogicalSideForUpdatingMovement()) {
            if (!(this.getFirstPassenger() instanceof PlayerEntity))
                this.setPaddleMovings(false, false);
            this.updateVelocity();
            if (isClient()) {
                this.updatePaddles();
                this.getWorld().sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
            }
            this.move(MovementType.SELF, this.getVelocity());
        }
        else {
            this.setVelocity(Vec3d.ZERO);
            this.setPaddleMovings(false, false);
        }
        this.handleBubbleColumn();
        for (int i = 0; i <= 1; ++i) {
            if (this.isPaddleMoving(i)) {
                SoundEvent soundEvent;
                Entity controllingPassenger = this.getControllingPassenger();
                if (!this.isSilent() && (double)(this.paddlePhases[i] % ((float)Math.PI * 2)) <= EMIT_SOUND_EVENT_PADDLE_ROTATION && (double)((this.paddlePhases[i] + NEXT_PADDLE_PHASE) % ((float)Math.PI * 2)) >= EMIT_SOUND_EVENT_PADDLE_ROTATION && (soundEvent = this.getPaddleSoundEvent()) != null)
                    this.getWorld().playSoundFromEntity(controllingPassenger instanceof PlayerEntity ? (PlayerEntity)controllingPassenger : null, this, soundEvent, this.getSoundCategory(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
                this.paddlePhases[i] = this.paddlePhases[i] + NEXT_PADDLE_PHASE;
                continue;
            }
            this.paddlePhases[i] = 0.0f;
        }
        this.checkBlockCollision();
        List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(0.2f, -0.01f, 0.2f), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            boolean nonPlayerController = !isClient() && !(this.getControllingPassenger() instanceof PlayerEntity);
            for (Entity entity : list) {
                if (entity.hasPassenger(this)) continue;
                if (nonPlayerController && this.getPassengerList().size() < this.getMaxPassengers() && !entity.hasVehicle() && this.isSmallerThanBoat(entity) && entity instanceof LivingEntity && !(entity instanceof WaterCreatureEntity) && !(entity instanceof PlayerEntity)) {
                    entity.startRiding(this);
                    continue;
                }
                this.pushAwayFrom(entity);
            }
        }
    }

    private boolean isClient() {
        return this.getWorld().isClient;
    }

    private void handleBubbleColumn() {
        if (isClient()) {
            int i = this.getBubbleWobbleTicks();
            this.bubbleWobbleStrength += i > 0 ? 0.05f : -0.1f;
            this.bubbleWobbleStrength = MathHelper.clamp(this.bubbleWobbleStrength, 0.0f, 1.0f);
            this.lastBubbleWobble = this.bubbleWobble;
            this.bubbleWobble = 10.0f * (float)Math.sin(0.5f * (float)this.getWorld().getTime()) * this.bubbleWobbleStrength;
        }
        else {
            int i;
            if (!this.onBubbleColumnSurface)
                this.setBubbleWobbleTicks(0);
            if ((i = this.getBubbleWobbleTicks()) > 0) {
                this.setBubbleWobbleTicks(--i);
                int j = 60 - i - 1;
                if (j > 0 && i == 0) {
                    this.setBubbleWobbleTicks(0);
                    Vec3d vec3d = this.getVelocity();
                    if (this.bubbleColumnIsDrag) {
                        this.setVelocity(vec3d.add(0.0, -0.7, 0.0));
                        this.removeAllPassengers();
                    }
                    else
                        this.setVelocity(vec3d.x, this.hasPassenger((Entity entity) -> entity instanceof PlayerEntity) ? 2.7 : 0.6, vec3d.z);
                }
                this.onBubbleColumnSurface = false;
            }
        }
    }

    @Nullable
    protected SoundEvent getPaddleSoundEvent() {
        switch (this.checkLocation()) {
            case IN_LAVA, UNDER_LAVA, UNDER_FLOWING_LAVA -> {
                return SoundRegister.ENTITY_BOAT_PADDLE_LAVA;
                // return SoundEvents.ENTITY_BOAT_PADDLE_WATER;
            }
            case ON_LAND -> {
                return SoundEvents.ENTITY_BOAT_PADDLE_LAND;
            }
        }
        return null;
    }

    private void updatePositionAndRotation() {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.field_7708 = 0;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
        }
        if (this.field_7708 <= 0)
            return;
        double d = this.getX() + (this.x - this.getX()) / (double)this.field_7708;
        double e = this.getY() + (this.y - this.getY()) / (double)this.field_7708;
        double f = this.getZ() + (this.z - this.getZ()) / (double)this.field_7708;
        double g = MathHelper.wrapDegrees(this.boatYaw - (double)this.getYaw());
        this.setYaw(this.getYaw() + (float)g / (float)this.field_7708);
        this.setPitch(this.getPitch() + (float)(this.boatPitch - (double)this.getPitch()) / (float)this.field_7708);
        --this.field_7708;
        this.setPosition(d, e, f);
        this.setRotation(this.getYaw(), this.getPitch());
    }

    public void setPaddleMovings(boolean leftMoving, boolean rightMoving) {
        this.dataTracker.set(LEFT_PADDLE_MOVING, leftMoving);
        this.dataTracker.set(RIGHT_PADDLE_MOVING, rightMoving);
    }

    public float interpolatePaddlePhase(int paddle, float tickDelta) {
        if (this.isPaddleMoving(paddle)) {
            return MathHelper.clampedLerp(this.paddlePhases[paddle] - NEXT_PADDLE_PHASE, this.paddlePhases[paddle], tickDelta);
        }
        return 0.0f;
    }

    private Location checkLocation() {
        Location location = this.getUnderLavaLocation();
        if (location != null) {
            this.lavaLevel = this.getBoundingBox().maxY;
            return location;
        }
        if (this.checkBoatInLava()) {
            return Location.IN_LAVA;
        }
        float f = this.getNearbySlipperiness();
        if (f > 0.0f) {
            this.nearbySlipperiness = f;
            return Location.ON_LAND;
        }
        return Location.IN_AIR;
    }

    public float getLavaHeightBelow() {
        Box box = this.getBoundingBox();
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.maxY);
        int l = MathHelper.ceil(box.maxY - this.fallVelocity);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        block0: for (int o = k; o < l; ++o) {
            float f = 0.0f;
            for (int p = i; p < j; ++p) {
                for (int q = m; q < n; ++q) {
                    mutable.set(p, o, q);
                    FluidState fluidState = this.getWorld().getFluidState(mutable);
                    if (fluidState.isIn(FluidTags.LAVA)) {
                        f = Math.max(f, fluidState.getHeight(this.getWorld(), mutable));
                    }
                    if (f >= 1.0f) continue block0;
                }
            }
            if (!(f < 1.0f)) continue;
            return (float)mutable.getY() + f;
        }
        return l + 1;
    }

    public float getNearbySlipperiness() {
        Box box = this.getBoundingBox();
        Box box2 = new Box(box.minX, box.minY - 0.001, box.minZ, box.maxX, box.minY, box.maxZ);
        int i = MathHelper.floor(box2.minX) - 1;
        int j = MathHelper.ceil(box2.maxX) + 1;
        int k = MathHelper.floor(box2.minY) - 1;
        int l = MathHelper.ceil(box2.maxY) + 1;
        int m = MathHelper.floor(box2.minZ) - 1;
        int n = MathHelper.ceil(box2.maxZ) + 1;
        VoxelShape voxelShape = VoxelShapes.cuboid(box2);
        float f = 0.0f;
        int o = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int p = i; p < j; ++p) {
            for (int q = m; q < n; ++q) {
                int r = (p == i || p == j - 1 ? 1 : 0) + (q == m || q == n - 1 ? 1 : 0);
                if (r == 2) continue;
                for (int s = k; s < l; ++s) {
                    if (r > 0 && (s == k || s == l - 1)) continue;
                    mutable.set(p, s, q);
                    BlockState blockState = this.getWorld().getBlockState(mutable);
                    if (blockState.getBlock() instanceof LilyPadBlock || !VoxelShapes.matchesAnywhere(blockState.getCollisionShape(this.getWorld(), mutable).offset(p, s, q), voxelShape, BooleanBiFunction.AND)) continue;
                    f += blockState.getBlock().getSlipperiness();
                    ++o;
                }
            }
        }
        return f / (float)o;
    }

    private boolean checkBoatInLava() {
        Box box = this.getBoundingBox();
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.ceil(box.minY + 0.001);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        boolean inLava = false;
        this.lavaLevel = -1.7976931348623157E308;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    mutable.set(o, p, q);
                    FluidState fluidState = this.getWorld().getFluidState(mutable);
                    if (!fluidState.isIn(FluidTags.LAVA)) continue;
                    float f = (float)p + fluidState.getHeight(this.getWorld(), mutable);
                    this.lavaLevel = Math.max(f, this.lavaLevel);
                    inLava |= box.minY < (double)f;
                }
            }
        }
        return inLava;
    }

    @Nullable
    private Location getUnderLavaLocation() {
        Box box = this.getBoundingBox();
        double d = box.maxY + 0.001;
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.maxY);
        int l = MathHelper.ceil(d);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        boolean stillFluid = false;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int o = i; o < j; ++o) {
            for (int p = k; p < l; ++p) {
                for (int q = m; q < n; ++q) {
                    mutable.set(o, p, q);
                    FluidState fluidState = this.getWorld().getFluidState(mutable);
                    if (!fluidState.isIn(FluidTags.LAVA) || !(d < (double)((float)mutable.getY() + fluidState.getHeight(this.getWorld(), mutable)))) continue;
                    if (fluidState.isStill()) {
                        stillFluid = true;
                        continue;
                    }
                    return Location.UNDER_FLOWING_LAVA;
                }
            }
        }
        return stillFluid ? Location.UNDER_LAVA : null;
    }

    private void updateVelocity() {
        double e = this.hasNoGravity() ? 0.0 : (double)-0.04f;
        double f = 0.0;
        float velocityDecay = 0.05f;
        if (this.lastLocation == Location.IN_AIR && this.location != Location.IN_AIR && this.location != Location.ON_LAND) {
            this.lavaLevel = this.getBodyY(1.0);
            this.setPosition(this.getX(), (double)(this.getLavaHeightBelow() - this.getHeight()) + 0.101, this.getZ());
            this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
            this.fallVelocity = 0.0;
            this.location = Location.IN_LAVA;
        } else {
            if (this.location == Location.IN_LAVA) {
                f = (this.lavaLevel - this.getY()) / (double)this.getHeight();
                velocityDecay = 0.9f;
            } else if (this.location == Location.UNDER_FLOWING_LAVA) {
                e = -7.0E-4;
                velocityDecay = 0.9f;
            } else if (this.location == Location.UNDER_LAVA) {
                f = 0.01f;
                velocityDecay = 0.45f;
            } else if (this.location == Location.IN_AIR) {
                velocityDecay = 0.9f;
            } else if (this.location == Location.ON_LAND) {
                velocityDecay = this.nearbySlipperiness;
                if (this.getControllingPassenger() instanceof PlayerEntity) {
                    this.nearbySlipperiness /= 2.0f;
                }
            }
            Vec3d vec3d = this.getVelocity();
            this.setVelocity(vec3d.x * (double) velocityDecay, vec3d.y + e, vec3d.z * (double) velocityDecay);
            this.yawVelocity *= velocityDecay;
            if (f > 0.0) {
                Vec3d vec3d2 = this.getVelocity();
                this.setVelocity(vec3d2.x, (vec3d2.y + f * 0.06153846016296973) * 0.75, vec3d2.z);
            }
        }
    }

    private void updatePaddles() {
        if (!this.hasPassengers()) {
            return;
        }
        float f = 0.0f;
        if (this.pressingLeft) {
            this.yawVelocity -= 1.0f;
        }
        if (this.pressingRight) {
            this.yawVelocity += 1.0f;
        }
        if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
            f += 0.005f;
        }
        this.setYaw(this.getYaw() + this.yawVelocity);
        if (this.pressingForward) {
            f += 0.04f;
        }
        if (this.pressingBack) {
            f -= 0.005f;
        }
        this.setVelocity(this.getVelocity().add(MathHelper.sin(-this.getYaw() * ((float)Math.PI / 180)) * f, 0.0, MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * f));
        this.setPaddleMovings(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
    }

    protected float getPassengerHorizontalOffset() {
        return 0.0f;
    }

    public boolean isSmallerThanBoat(Entity entity) {
        return entity.getWidth() < this.getWidth();
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float f = this.getPassengerHorizontalOffset();
        float g = (float)((this.isRemoved() ? (double)0.01f : this.getMountedHeightOffset()) + passenger.getHeightOffset());
        if (this.getPassengerList().size() > 1) {
            int i = this.getPassengerList().indexOf(passenger);
            f = i == 0 ? 0.2f : -0.6f;
            if (passenger instanceof AnimalEntity) {
                f += 0.2f;
            }
        }
        Vec3d vec3d = new Vec3d(f, 0.0, 0.0).rotateY(-this.getYaw() * ((float)Math.PI / 180) - 1.5707964f);
        positionUpdater.accept(passenger, this.getX() + vec3d.x, this.getY() + (double)g, this.getZ() + vec3d.z);
        passenger.setYaw(passenger.getYaw() + this.yawVelocity);
        passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);
        this.copyEntityData(passenger);
        if (passenger instanceof AnimalEntity && this.getPassengerList().size() == this.getMaxPassengers()) {
            int j = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)j);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)j);
        }
    }

    private boolean isBlockLava(BlockPos blockPos) {
        return this.getWorld().getFluidState(blockPos).isIn(FluidTags.LAVA);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d vec3d = getPassengerDismountOffset(this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO, passenger.getWidth(), passenger.getYaw());
        double d = this.getX() + vec3d.x;
        double e = this.getZ() + vec3d.z;
        BlockPos blockPos = BlockPos.ofFloored(d, this.getBoundingBox().maxY, e);
        BlockPos blockPos2 = blockPos.down();
        if (!isBlockLava(blockPos2)) {
            double g;
            ArrayList<Vec3d> list = Lists.newArrayList();
            double f = this.getWorld().getDismountHeight(blockPos);
            if (Dismounting.canDismountInBlock(f)) {
                list.add(new Vec3d(d, (double)blockPos.getY() + f, e));
            }
            if (Dismounting.canDismountInBlock(g = this.getWorld().getDismountHeight(blockPos2))) {
                list.add(new Vec3d(d, (double)blockPos2.getY() + g, e));
            }
            for (EntityPose entityPose : passenger.getPoses()) {
                for (Vec3d vec3d2 : list) {
                    if (!Dismounting.canPlaceEntityAt(this.getWorld(), vec3d2, passenger, entityPose)) continue;
                    passenger.setPose(entityPose);
                    return vec3d2;
                }
            }
        }
        return super.updatePassengerForDismount(passenger);
    }

    protected void copyEntityData(Entity entity) {
        entity.setBodyYaw(this.getYaw());
        float f = MathHelper.wrapDegrees(entity.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        entity.prevYaw += g - f;
        entity.setYaw(entity.getYaw() + g - f);
        entity.setHeadYaw(entity.getYaw());
    }

    @Override
    public void onPassengerLookAround(Entity passenger) {
        this.copyEntityData(passenger);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("Type", this.getVariant().asString());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Type", NbtElement.STRING_TYPE)) {
            this.setVariant(BoatEntity.Type.getType(nbt.getString("Type")));
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction())
            return ActionResult.PASS;
        if (this.ticksUnderlava < 60.0f) {
            if (!isClient())
                return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        this.fallVelocity = this.getVelocity().y;
        if (this.hasVehicle())
            return;
        if (onGround) {
            if (this.fallDistance > 3.0f) {
                if (this.location != Location.ON_LAND) {
                    this.onLanding();
                    return;
                }
                this.handleFallDamage(this.fallDistance, 1.0f, this.getDamageSources().fall());
                if (!isClient() && !this.isRemoved()) {
                    this.kill();
                    if (this.getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                        int i;
                        for (i = 0; i < 3; ++i) {
                            this.dropItem(this.getVariant().getBaseBlock());
                        }
                        for (i = 0; i < 2; ++i) {
                            this.dropItem(Items.STICK);
                        }
                    }
                }
            }
            this.onLanding();
        }
        else if (!this.getWorld().getFluidState(this.getBlockPos().down()).isIn(FluidTags.WATER) && heightDifference < 0.0) {
            this.fallDistance -= (float)heightDifference;
        }
    }

    public boolean isPaddleMoving(int paddle) {
        return this.dataTracker.get(paddle == 0 ? LEFT_PADDLE_MOVING : RIGHT_PADDLE_MOVING) && this.getControllingPassenger() != null;
    }

    public void setDamageWobbleStrength(float wobbleStrength) {
        this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
    }

    public float getDamageWobbleStrength() {
        return this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
    }

    public void setDamageWobbleTicks(int wobbleTicks) {
        this.dataTracker.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
    }

    public int getDamageWobbleTicks() {
        return this.dataTracker.get(DAMAGE_WOBBLE_TICKS);
    }

    private void setBubbleWobbleTicks(int wobbleTicks) {
        this.dataTracker.set(BUBBLE_WOBBLE_TICKS, wobbleTicks);
    }

    private int getBubbleWobbleTicks() {
        return this.dataTracker.get(BUBBLE_WOBBLE_TICKS);
    }

    public float interpolateBubbleWobble(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastBubbleWobble, this.bubbleWobble);
    }

    public void setDamageWobbleSide(int side) {
        this.dataTracker.set(DAMAGE_WOBBLE_SIDE, side);
    }

    public int getDamageWobbleSide() {
        return this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
    }

    @Override
    public void setVariant(BoatEntity.Type type) {
        this.dataTracker.set(BOAT_TYPE, type.ordinal());
    }

    @Override
    public BoatEntity.Type getVariant() {
        return BoatEntity.Type.getType(this.dataTracker.get(BOAT_TYPE));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < this.getMaxPassengers() && !this.isSubmergedIn(FluidTags.WATER);
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }

    public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack) {
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
    }

    public boolean isSubmergedInLava() {
        return this.location == Location.UNDER_LAVA || this.location == Location.UNDER_FLOWING_LAVA;
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(this.asItem());
    }

    public enum Location {
        IN_LAVA,
        UNDER_LAVA,
        UNDER_FLOWING_LAVA,
        ON_LAND,
        IN_AIR
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }
}
