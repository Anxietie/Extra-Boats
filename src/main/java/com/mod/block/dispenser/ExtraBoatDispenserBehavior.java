package com.mod.block.dispenser;

import com.mod.entity.ExtraBoatEntity;
import com.mod.entity.ExtraChestBoatEntity;
import com.mod.registry.EntityRegister;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ExtraBoatDispenserBehavior extends ItemDispenserBehavior {
    private final ItemDispenserBehavior itemDispenser = new ItemDispenserBehavior();
    private final BoatEntity.Type boatType;
    private final boolean chest;

    public ExtraBoatDispenserBehavior(BoatEntity.Type type) {
        this(type, false);
    }

    public ExtraBoatDispenserBehavior(BoatEntity.Type boatType, boolean chest) {
        this.boatType = boatType;
        this.chest = chest;
    }

    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        double h;
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        ServerWorld world = pointer.getWorld();
        double d = 0.5625 + (double)EntityRegister.EXTRA_BOAT.getWidth() / 2.0;
        double e = pointer.getX() + (double)direction.getOffsetX() * d;
        double f = pointer.getY() + (double)((float)direction.getOffsetY() * 1.125f);
        double g = pointer.getZ() + (double)direction.getOffsetZ() * d;
        BlockPos blockPos = pointer.getPos().offset(direction);
        if (world.getFluidState(blockPos).isIn(FluidTags.LAVA))
            h = 1.0;
        else if (world.getBlockState(blockPos).isAir() && world.getFluidState(blockPos.down()).isIn(FluidTags.LAVA))
            h = 0.0;
        else
            return this.itemDispenser.dispense(pointer, stack);
        ExtraBoatEntity boatEntity = this.chest ? new ExtraChestBoatEntity(world, e, f + h, g) : new ExtraBoatEntity(world, e, f + h, g);
        boatEntity.setVariant(this.boatType);
        boatEntity.setYaw(direction.asRotation());
        world.spawnEntity(boatEntity);
        stack.decrement(1);
        return stack;
    }
}
